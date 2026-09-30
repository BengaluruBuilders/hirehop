package com.hirehop.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import org.junit.Test

class OfflineGapMatcherTest {
    private val analyzer = OfflineJobDescriptionAnalyzer()
    private val matcher = OfflineGapMatcher()

    private fun analyse(jd: String, profile: CandidateProfile = sampleProfile): Pair<JobDescription, GapAnalysis> {
        val job = analyzer.analyze(jd)
        return job to matcher.match(profile, job)
    }

    private fun GapAnalysis.forKeyword(keyword: String): RequirementMatch =
        matches.first { keyword in it.requirement.keywords }

    @Test
    fun trapJobMarksMissingSkillsAsGap() {
        val (_, gap) = analyse(resourceText("jd_trap_cloud.txt"))
        listOf("mba", "kubernetes", "aws", "terraform").forEach {
            assertThat(gap.forKeyword(it).status).isEqualTo(MatchStatus.GAP)
            assertThat(gap.forKeyword(it).evidenceIds).isEmpty()
        }
        assertThat(gap.forKeyword("kotlin").status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun unconfirmedEntriesAreNotEvidence() {
        val (_, gap) = analyse("Requirements\n- Experience with Kubernetes on AWS")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.GAP)
    }

    @Test
    fun confirmedEntryBecomesEvidence() {
        val confirmed = sampleProfile.copy(
            entries = sampleProfile.entries.map { if (it.id == "unconfirmed-1") it.copy(isConfirmed = true) else it },
        )
        val (_, gap) = analyse("Requirements\n- Experience with Kubernetes on AWS", confirmed)
        val match = gap.matches.single()
        assertThat(match.status).isEqualTo(MatchStatus.MET)
        assertThat(match.evidenceIds).containsExactly("unconfirmed-1", "unconfirmed-1-b1").inOrder()
    }

    @Test
    fun allKeywordsEvidencedIsMetAndSomeIsPartial() {
        val (_, gap) = analyse("Requirements\n- Kotlin and Spring Boot\n- Python and Kubernetes\n- Kubernetes")
        assertThat(gap.matches.map { it.status })
            .containsExactly(MatchStatus.MET, MatchStatus.PARTIAL, MatchStatus.GAP).inOrder()
    }

    @Test
    fun evidenceIdsReferenceBulletsEntryTitlesAndSkills() {
        val (_, gap) = analyse("Requirements\n- Kotlin\n- Jetpack Compose\n- Git")
        assertThat(gap.matches[0].evidenceIds).containsExactly("exp-1-b1", "skill:Kotlin").inOrder()
        assertThat(gap.matches[1].evidenceIds).containsExactly("proj-1-b1")
        assertThat(gap.matches[2].evidenceIds).containsExactly("exp-1-b3", "skill:Git").inOrder()
    }

    @Test
    fun entryTitleCountsAsEvidenceAndCitesTheEntryId() {
        val (_, gap) = analyse("Requirements\n- Experience with Android")
        assertThat(gap.matches.single().evidenceIds).containsAtLeast("proj-1", "proj-1-b1")
    }

    @Test
    fun profileSkillAloneIsEvidence() {
        val profile = profileOf(listOf("K8s"))
        val (_, gap) = analyse("Requirements\n- Kubernetes", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.MET)
        assertThat(gap.matches.single().evidenceIds).containsExactly("skill:K8s")
    }

    @Test
    fun aliasesInEvidenceMatchCanonicalRequirementKeywords() {
        val (_, gap) = analyse("Requirements\n- JavaScript\n- Excel")
        assertThat(gap.matches.map { it.status }).containsExactly(MatchStatus.MET, MatchStatus.MET)
    }

    @Test
    fun genericDegreeIsMetByASpecificDegree() {
        val (_, gap) = analyse("Requirements\n- Bachelor's degree in Computer Science")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun educationAlternativesNeedOnlyOneMatch() {
        val (_, gap) = analyse("Requirements\n- B.Tech / B.E. / BCA in Computer Science")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun missingSpecificDegreeIsGap() {
        val (_, gap) = analyse("Requirements\n- MBA in Finance")
        assertThat(gap.forKeyword("mba").status).isEqualTo(MatchStatus.GAP)
    }

    @Test
    fun keywordCoverageCountsDistinctKeywordsAcrossRequirements() {
        val (_, gap) = analyse("Requirements\n- Kotlin and Kubernetes\n- Kotlin and Terraform\n- Git")
        assertThat(gap.keywordCoverage.total).isEqualTo(4)
        assertThat(gap.keywordCoverage.covered).isEqualTo(2)
    }

    @Test
    fun matchesKeepAnalyzerOrder() {
        val (job, gap) = analyse(resourceText("jd_android.txt"))
        assertThat(gap.matches.map { it.requirement }).containsExactlyElementsIn(job.requirements).inOrder()
    }

    @Test
    fun requirementWithoutKeywordsIsGapWithoutEvidence() {
        val (_, gap) = analyse("Requirements\n- Should have 2 years of experience")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.GAP)
        assertThat(gap.matches.single().evidenceIds).isEmpty()
        assertThat(gap.keywordCoverage.total).isEqualTo(0)
    }

    @Test
    fun nonLexiconKeywordsMatchByStem() {
        val profile = profileOf(
            emptyList(),
            entry("e1", EntryCategory.EXPERIENCE, "Support Intern", "Handled customer complaints over phone"),
        )
        val (_, gap) = analyse("Requirements\n- Prior experience handling customer complaints", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.PARTIAL)
        assertThat(gap.matches.single().evidenceIds).containsExactly("e1-b1")
    }

    @Test
    fun cRequirementIsNotSatisfiedByCppEvidence() {
        val profile = profileOf(listOf("C++"))
        val (_, gap) = analyse("Requirements\n- Knowledge of C, Python", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.GAP)
    }

    @Test
    fun javaRequirementIsNotSatisfiedByJavaScriptEvidence() {
        val profile = profileOf(listOf("JavaScript"))
        val (_, gap) = analyse("Requirements\n- Java", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.GAP)
    }

    @Test
    fun genericDegreeWithFieldIsNotMetByADegreeInAnotherField() {
        val profile = profileOf(emptyList(), entry("e1", EntryCategory.EDUCATION, "B.Com in Finance"))
        val (_, gap) = analyse("Requirements\n- Bachelor's degree in Computer Science", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.PARTIAL)
    }

    @Test
    fun genericDegreeWithFieldIsMetByADegreeInThatField() {
        val profile = profileOf(emptyList(), entry("e1", EntryCategory.EDUCATION, "B.Com in Finance"))
        val (_, gap) = analyse("Requirements\n- Bachelor's degree in Commerce", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun fieldAlternativesNeedOnlyOneField() {
        val (_, gap) = analyse("Requirements\n- B.Tech in Computer Science or Information Technology")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun missingFieldOfStudyWithMatchingLevelIsPartial() {
        val profile = profileOf(emptyList(), entry("e1", EntryCategory.EDUCATION, "B.Tech in Mechanical Engineering"))
        val (_, gap) = analyse("Requirements\n- B.Tech in Computer Science", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.PARTIAL)
    }

    @Test
    fun yearsRequirementIsAtMostPartialWithAProjectOnlyResume() {
        val profile = profileOf(
            emptyList(),
            entry("p1", EntryCategory.PROJECT, "Quiz App", "Built a Java quiz app"),
        )
        val (_, gap) = analyse("Requirements\n- 5+ years of Java experience", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.PARTIAL)
        assertThat(gap.matches.single().evidenceIds).contains("p1-b1")
    }

    @Test
    fun yearsRequirementWithNoEvidenceStaysGap() {
        val (_, gap) = analyse("Requirements\n- 3 years of Kubernetes experience")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.GAP)
    }

    @Test
    fun freshersRangeStartingAtZeroYearsCanBeMet() {
        val (_, gap) = analyse("Requirements\n- 0-2 years of Kotlin experience")
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun excelVerbInResumeIsNotEvidenceForExcelTool() {
        val profile = profileOf(
            emptyList(),
            entry("e1", EntryCategory.ACHIEVEMENT, "Awards", "Students who excel in academics get medals"),
        )
        val (_, gap) = analyse("Requirements\n- Excel", profile)
        assertThat(gap.matches.single().status).isEqualTo(MatchStatus.GAP)
    }
}
