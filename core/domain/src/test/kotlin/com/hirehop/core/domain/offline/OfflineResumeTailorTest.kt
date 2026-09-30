package com.hirehop.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import org.junit.Test

class OfflineResumeTailorTest {
    private val analyzer = OfflineJobDescriptionAnalyzer()
    private val matcher = OfflineGapMatcher()
    private val tailor = OfflineResumeTailor()

    private fun tailorFor(
        jd: String,
        profile: CandidateProfile = sampleProfile,
    ): Pair<JobDescription, TailoredResume> {
        val job = analyzer.analyze(jd)
        return job to tailor.tailor(profile, job, matcher.match(profile, job))
    }

    private fun TailoredResume.bulletFor(sourceId: String): TailoredBullet = bullets.first { sourceId in it.sourceIds }

    private fun tokensOf(text: String) = TextTokens.words(text)

    @Test
    fun onlyConfirmedEntriesAreTailoredAndEveryBulletIsCovered() {
        val (_, resume) = tailorFor("Requirements\n- Kotlin")
        val expected = sampleProfile.entries.filter { it.isConfirmed }.flatMap { it.bullets }.map { it.id }
        assertThat(resume.bullets.flatMap { it.sourceIds }).containsExactlyElementsIn(expected)
    }

    @Test
    fun bulletsStartPendingWithSourceIdsAndNoViolations() {
        val (_, resume) = tailorFor(resourceText("jd_android.txt"))
        resume.bullets.forEach {
            assertThat(it.decision).isEqualTo(BulletDecision.PENDING)
            assertThat(it.violations).isEmpty()
            assertThat(it.sourceIds).hasSize(1)
            assertThat(it.id).isEqualTo("tailored-${it.sourceIds.single()}")
        }
    }

    @Test
    fun trapJobGapsNeverAppearInAnyTailoredBullet() {
        val (job, resume) = tailorFor(resourceText("jd_trap_cloud.txt"))
        val forbidden = listOf("kubernetes", "aws", "mba", "terraform", "docker")
        resume.bullets.forEach { bullet ->
            val words = tokensOf(bullet.proposedText)
            forbidden.forEach { assertThat(words).doesNotContain(it) }
            assertThat(bullet.keywordsUsed).containsNoneIn(forbidden)
        }
        assertThat(job.requirements.flatMap { it.keywords }).containsAtLeastElementsIn(forbidden)
    }

    @Test
    fun rewordsAliasToTheJobTermOnlyWhenTheBulletHasTheAlias() {
        val (_, resume) = tailorFor("Requirements\n- JavaScript\n- Excel")
        val js = resume.bulletFor("exp-1-b4")
        assertThat(js.proposedText).isEqualTo("Fixing bugs in the JavaScript frontend")
        assertThat(js.editTypes).containsAtLeast(EditType.REWORD, EditType.SHORTEN)
        assertThat(js.editTypes).doesNotContain(EditType.EMPHASISE)
        assertThat(js.keywordsUsed).contains("javascript")
        val excel = resume.bulletFor("proj-1-b3")
        assertThat(excel.proposedText).isEqualTo("Created charts in Excel to analyse spending data")
        assertThat(excel.editTypes).contains(EditType.REWORD)
        assertThat(excel.editTypes).doesNotContain(EditType.SHORTEN)
        assertThat(resume.bulletFor("exp-1-b2").proposedText).isEqualTo("Wrote unit tests with JUnit to improve reliability")
    }

    @Test
    fun pluralFormOfTheSameTermIsNotReworded() {
        val (_, resume) = tailorFor("Requirements\n- REST API")
        assertThat(resume.bulletFor("exp-1-b1").proposedText).contains("REST APIs")
    }

    @Test
    fun aliasIsNotRewordedWhenJobDoesNotAskForIt() {
        val (_, resume) = tailorFor("Requirements\n- Kotlin")
        assertThat(resume.bulletFor("exp-1-b4").proposedText).contains("js frontend")
        assertThat(resume.bulletFor("proj-1-b3").proposedText).contains("ms excel")
    }

    @Test
    fun looseAliasesAreNeverReworded() {
        val profile = profileOf(
            emptyList(),
            entry("e1", EntryCategory.EXPERIENCE, "Intern", "Wrote core java services for billing"),
        )
        val (_, resume) = tailorFor("Requirements\n- Java", profile)
        assertThat(resume.bullets.single().proposedText).isEqualTo("Wrote core java services for billing")
        assertThat(resume.bullets.single().editTypes).isEmpty()
        assertThat(resume.bullets.single().keywordsUsed).containsExactly("java")
    }

    @Test
    fun shortensFillerPhrasesWithoutAddingWords() {
        val (_, resume) = tailorFor("Requirements\n- Kotlin")
        val bullet = resume.bulletFor("exp-1-b1")
        assertThat(bullet.proposedText)
            .isEqualTo("Using Kotlin and Spring Boot, building REST APIs for the inventory module")
        assertThat(bullet.editTypes).containsExactly(EditType.SHORTEN, EditType.EMPHASISE).inOrder()
    }

    @Test
    fun emphasisesKeywordClauseByPureReordering() {
        val (_, resume) = tailorFor("Requirements\n- Jetpack Compose")
        val bullet = resume.bulletFor("proj-1-b1")
        assertThat(bullet.proposedText)
            .isEqualTo("Using Jetpack Compose and Room, built an Android app to track daily expenses")
        assertThat(bullet.editTypes).contains(EditType.EMPHASISE)
        assertThat(tokensOf(bullet.proposedText).sorted()).isEqualTo(tokensOf(bullet.originalText).sorted())
    }

    @Test
    fun doesNotEmphasiseWhenKeywordAlreadyLeads() {
        val profile = profileOf(
            emptyList(),
            entry("e1", EntryCategory.PROJECT, "App", "Kotlin developer who built tools using Git"),
        )
        val (_, resume) = tailorFor("Requirements\n- Kotlin\n- Git", profile)
        assertThat(resume.bullets.single().editTypes).doesNotContain(EditType.EMPHASISE)
    }

    @Test
    fun doesNotEmphasiseWhenClauseContinuesWithAnotherVerb() {
        val profile = profileOf(
            emptyList(),
            entry("e1", EntryCategory.PROJECT, "App", "Built dashboards using Excel and presented insights weekly"),
        )
        val (_, resume) = tailorFor("Requirements\n- Excel", profile)
        assertThat(resume.bullets.single().proposedText).isEqualTo("Built dashboards using Excel and presented insights weekly")
    }

    @Test
    fun reordersBulletsWithinAnEntryByJobRelevance() {
        val (_, resume) = tailorFor("Requirements\n- Git")
        val experience = resume.bullets.filter { it.entryId == "exp-1" }
        assertThat(experience.map { it.sourceIds.single() })
            .containsExactly("exp-1-b3", "exp-1-b1", "exp-1-b2", "exp-1-b4").inOrder()
        assertThat(experience[0].editTypes).contains(EditType.REORDER)
        assertThat(experience[3].editTypes).doesNotContain(EditType.REORDER)
    }

    @Test
    fun entriesKeepProfileOrder() {
        val (_, resume) = tailorFor("Requirements\n- Power BI")
        assertThat(resume.bullets.map { it.entryId }.distinct())
            .containsExactly("edu-1", "exp-1", "proj-1", "proj-2").inOrder()
    }

    @Test
    fun mustHaveKeywordsOutrankNiceToHaveWhenReordering() {
        val profile = profileOf(
            emptyList(),
            entry(
                "e1",
                EntryCategory.EXPERIENCE,
                "Intern",
                "Used Tableau for reports",
                "Used SQL for reports",
            ),
        )
        val (_, resume) = tailorFor("Requirements\n- SQL\nPreferred\n- Tableau", profile)
        assertThat(resume.bullets.map { it.sourceIds.single() }).containsExactly("e1-b2", "e1-b1").inOrder()
    }

    @Test
    fun unchangedBulletHasNoEditTypes() {
        val (_, resume) = tailorFor("Requirements\n- Kotlin")
        val bullet = resume.bulletFor("exp-1-b3")
        assertThat(bullet.proposedText).isEqualTo(bullet.originalText)
        assertThat(bullet.editTypes).isEmpty()
    }

    @Test
    fun keywordsUsedListsJobKeywordsPresentInProposedText() {
        val (_, resume) = tailorFor("Requirements\n- Kotlin, Python and SQL")
        assertThat(resume.bulletFor("exp-1-b1").keywordsUsed).containsExactly("kotlin")
        assertThat(resume.bulletFor("proj-2-b1").keywordsUsed).containsExactly("sql")
        assertThat(resume.bulletFor("edu-1-b1").keywordsUsed).isEmpty()
    }

    @Test
    fun tailoredTextNeverIntroducesWordsOutsideTheSourceBulletAcrossFiveJobs() {
        jobDescriptionResources.forEach { resource ->
            val (job, resume) = tailorFor(resourceText(resource))
            val jobTerms = job.requirements.flatMap { it.keywords }.distinct()
            val allowedExtras = jobTerms.flatMap { tokensOf(SkillLexicon.displayName(it)) }.toSet()
            resume.bullets.forEach { bullet ->
                val allowed = tokensOf(bullet.originalText).toSet() + allowedExtras
                val extras = tokensOf(bullet.proposedText).filterNot { it in allowed }
                assertThat(extras).isEmpty()
                assertThat(jobTerms).containsAtLeastElementsIn(bullet.keywordsUsed)
            }
        }
    }

    @Test
    fun aliasRewordingIntroducesOnlyTheCanonicalTermTokens() {
        val (_, resume) = tailorFor("Requirements\n- JavaScript\n- Excel")
        resume.bullets.forEach { bullet ->
            val allowed = tokensOf(bullet.originalText).toSet() + tokensOf("JavaScript Excel")
            assertThat(tokensOf(bullet.proposedText).filterNot { it in allowed }).isEmpty()
        }
    }

    @Test
    fun tailoringIsDeterministic() {
        val jd = resourceText("jd_data_analyst.txt")
        assertThat(tailorFor(jd).second).isEqualTo(tailorFor(jd).second)
    }

    @Test
    fun profileWithoutConfirmedEntriesGivesEmptyResume() {
        val profile = sampleProfile.copy(entries = sampleProfile.entries.map { it.copy(isConfirmed = false) })
        assertThat(tailorFor("Requirements\n- Kotlin", profile).second.bullets).isEmpty()
    }

    @Test
    fun fillerRemoverKeepsShortBulletsIntact() {
        assertThat(FillerRemover.shorten("Various tasks")).isEqualTo("Various tasks")
        assertThat(FillerRemover.shorten("Responsible for various reports and dashboards"))
            .isEqualTo("Reports and dashboards")
        assertThat(FillerRemover.shorten("Studied in order to learn Kotlin")).isEqualTo("Studied to learn Kotlin")
    }
}
