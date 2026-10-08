package com.tailormyresume.core.domain.coverletter

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalCoroutineRequirement
import com.tailormyresume.core.testing.data.canonicalGapAnalysis
import com.tailormyresume.core.testing.data.canonicalGitRequirement
import com.tailormyresume.core.testing.data.canonicalJobDescription
import com.tailormyresume.core.testing.data.canonicalKotlinRequirement
import com.tailormyresume.core.testing.data.canonicalProfileWithoutEntries
import com.tailormyresume.core.testing.data.canonicalRetrofitRequirement
import com.tailormyresume.core.testing.data.canonicalRoomRequirement
import org.junit.Test

class CoverLetterComposerTest {
    private val analysis = JobAnalysisResult(canonicalJobDescription, canonicalGapAnalysis)

    @Test
    fun namesTheRealRoleAndCompanyInTheOpening() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis)

        assertThat(draft.greeting).isEqualTo("Dear hiring team,")
        assertThat(draft.openingParagraph).contains("I am applying for the Associate Android Engineer role at Northwind GCC.")
        assertThat(draft.openingParagraph).contains("My confirmed background covers 6 of the 10 requirements in the posting.")
    }

    @Test
    fun evidenceParagraphQuotesConfirmedProfileFactsOnly() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis)

        assertThat(draft.evidenceParagraph).contains("Here is the work I can show, taken from my own record.")
        assertThat(draft.evidenceParagraph).contains("Kotlin")
        assertThat(draft.evidenceParagraph).contains("Jetpack Compose")
        assertThat(draft.evidenceParagraph).doesNotContain("I have no confirmed evidence")
    }

    @Test
    fun theSameSentenceIsNeverCitedTwice() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis, 6)

        val facts = draft.evidenceParagraph.removePrefix(CoverLetterComposer.EVIDENCE_LEAD_IN).trim()

        assertThat(facts).isEqualTo(
            "Kotlin, Java, Android SDK, Jetpack Compose, Room, Coroutines, Flow, Retrofit, SQL, Git.",
        )
    }

    @Test
    fun evidenceParagraphUsesAtMostTheRequestedNumberOfFacts() {
        val wide = JobAnalysisResult(canonicalJobDescription, wideGap())

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, wide, 2)

        val facts = draft.evidenceParagraph.removePrefix(CoverLetterComposer.EVIDENCE_LEAD_IN).trim()
        val clauses = facts.split("; ")

        assertThat(clauses).hasSize(2)
        assertThat(clauses.last()).endsWith(".")
    }

    @Test
    fun threeDistinctFactsAreCitedByDefault() {
        val wide = JobAnalysisResult(canonicalJobDescription, wideGap())

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, wide)

        val facts = draft.evidenceParagraph.removePrefix(CoverLetterComposer.EVIDENCE_LEAD_IN).trim()
        assertThat(facts.split("; ")).hasSize(CoverLetterComposer.DEFAULT_MAX_EVIDENCE)
    }

    @Test
    fun profileWithoutMatchingEvidenceSaysSoHonestly() {
        val draft = CoverLetterComposer.compose(canonicalProfileWithoutEntries, canonicalJobDescription, analysis)

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
        assertThat(draft.evidenceParagraph).doesNotContain("Kotlin")
        assertThat(draft.evidenceParagraph).doesNotContain("Jetpack Compose")
        assertThat(draft.evidenceParagraph).doesNotContain("Room")
    }

    @Test
    fun unconfirmedEvidenceIsNeverQuoted() {
        val unconfirmed = canonicalCandidateProfile.copy(
            entries = canonicalCandidateProfile.entries.map { entry -> entry.copy(isConfirmed = false) },
        )

        val draft = CoverLetterComposer.compose(unconfirmed, canonicalJobDescription, analysis)

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
    }

    @Test
    fun analysisWithoutAnyMatchKeepsTheHonestFallback() {
        val noMatches = GapAnalysis(emptyList(), KeywordCoverage(covered = 0, total = 0))
        val bare = JobAnalysisResult(canonicalJobDescription, noMatches)

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, bare)

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
        assertThat(draft.openingParagraph).isEqualTo(
            "I am applying for the Associate Android Engineer role at Northwind GCC.",
        )
    }

    @Test
    fun missingTitleAndCompanyOmitTheSentence() {
        val bare = canonicalJobDescription.copy(title = "  ", company = "")

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, bare, analysis)

        assertThat(draft.openingParagraph).doesNotContain("applying")
        assertThat(draft.openingParagraph).contains("My confirmed background covers 6 of the 10 requirements")
    }

    @Test
    fun missingCompanyKeepsTheTitle() {
        val bare = canonicalJobDescription.copy(company = "   ")

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, bare, analysis)

        assertThat(draft.openingParagraph).contains("I am applying for the Associate Android Engineer role.")
        assertThat(draft.openingParagraph).doesNotContain("Northwind")
    }

    @Test
    fun missingNameOmitsTheSignOff() {
        val anonymous = canonicalCandidateProfile.copy(fullName = " ")

        val draft = CoverLetterComposer.compose(anonymous, canonicalJobDescription, analysis)

        assertThat(draft.closingParagraph).isEqualTo("Thank you for reading my application.")
    }

    @Test
    fun signOffUsesTheCandidateOwnName() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis)

        assertThat(draft.closingParagraph).contains("Yours sincerely, Priya Deshmukh.")
    }

    @Test
    fun noTemplateTokenSurvivesIntoTheOutput() {
        val tokenJob = canonicalJobDescription.copy(
            title = "{{title}} [Job Title] %s",
            company = "<COMPANY> {company}",
        )

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, tokenJob, analysis)

        val output = draft.asText()
        assertThat(output).doesNotContain("{")
        assertThat(output).doesNotContain("}")
        assertThat(output).doesNotContain("[")
        assertThat(output).doesNotContain("]")
        assertThat(output).doesNotContain("<")
        assertThat(output).doesNotContain(">")
        assertThat(output).doesNotContain("%s")
    }

    @Test
    fun noLiteralPlaceholderSurvivesIntoTheOutput() {
        val tokenJob = canonicalJobDescription.copy(
            title = "[Job Title] {role} <position> %s",
            company = "{{COMPANY_NAME}}",
        )

        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, tokenJob, analysis)
        val output = draft.asText()

        assertThat(output).doesNotContain("[Job Title]")
        assertThat(output).doesNotContain("{role}")
        assertThat(output).doesNotContain("<position>")
        assertThat(output).doesNotContain("{{")
        assertThat(output).contains("I am applying for the Job Title role position s role at COMPANY_NAME.")
    }

    @Test
    fun inventsNoEmployerContactName() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis)

        assertThat(draft.greeting).isEqualTo("Dear hiring team,")
        assertThat(draft.asText()).doesNotContain("Dear Mr")
        assertThat(draft.asText()).doesNotContain("Dear Ms")
        assertThat(draft.asText()).doesNotContain("Dear Dr")
        assertThat(draft.asText()).doesNotContain("hiring manager")
    }

    @Test
    fun theOnlyPersonNamedIsTheCandidate() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis)

        assertThat(draft.openingParagraph).doesNotContain("Priya")
        assertThat(draft.evidenceParagraph).doesNotContain("Priya")
        assertThat(draft.closingParagraph).contains("Priya Deshmukh")
    }

    @Test
    fun inventsNoSkillTheProfileDoesNotHave() {
        val kubernetes = JobRequirement(
            id = "req-kubernetes",
            text = "Kubernetes cluster operations",
            type = RequirementType.TOOL,
            priority = RequirementPriority.NICE_TO_HAVE,
            keywords = listOf("kubernetes"),
        )
        val gap = GapAnalysis(
            matches = listOf(RequirementMatch(kubernetes, MatchStatus.MET, listOf("I-01-b1"))),
            keywordCoverage = KeywordCoverage(covered = 0, total = 1),
        )
        val job = canonicalJobDescription.copy(requirements = canonicalJobDescription.requirements + kubernetes)

        val draft = CoverLetterComposer.compose(
            candidate = canonicalCandidateProfile,
            job = job,
            analysis = JobAnalysisResult(job, gap),
        )

        assertThat(draft.asText()).doesNotContain("Kubernetes")
    }

    @Test
    fun onlyRequirementTheProfileActuallyStatesIsQuoted() {
        val partialOnly = canonicalRetrofitRequirement
        val gap = GapAnalysis(
            matches = listOf(RequirementMatch(partialOnly, MatchStatus.PARTIAL, listOf("I-02-b2"))),
            keywordCoverage = KeywordCoverage(covered = 0, total = 1),
        )

        val draft = CoverLetterComposer.compose(
            candidate = canonicalCandidateProfile,
            job = canonicalJobDescription,
            analysis = JobAnalysisResult(canonicalJobDescription, gap),
        )

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
    }

    @Test
    fun evidenceIsTakenOnlyFromConfirmedEntriesThatStateTheRequirement() {
        val job = JobDescription("Android engineer", "Northwind", "raw", emptyList())

        val draft = CoverLetterComposer.compose(
            candidate = canonicalCandidateProfile,
            job = job,
            analysis = JobAnalysisResult(job, canonicalGapAnalysis),
            maxEvidence = 6,
        )

        val facts = draft.evidenceParagraph.removePrefix("Here is the work I can show, taken from my own record.")
        assertThat(facts.split("; ")).isNotEmpty()
        assertThat(facts).doesNotContain("BigQuery")
    }

    @Test
    fun zeroEvidenceBudgetFallsBackHonestly() {
        val draft = CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis, 0)

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
    }

    @Test
    fun emptyCandidateProfileStillProducesAnHonestLetter() {
        val empty = CandidateProfile("", "", "", "", emptyList(), emptyList())

        val draft = CoverLetterComposer.compose(empty, canonicalJobDescription, analysis)

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
        assertThat(draft.closingParagraph).isEqualTo("Thank you for reading my application.")
    }

    private fun wideGap(): GapAnalysis = GapAnalysis(
        matches = listOf(
            RequirementMatch(canonicalKotlinRequirement, MatchStatus.MET, listOf("I-01-b1")),
            RequirementMatch(canonicalCoroutineRequirement, MatchStatus.MET, listOf("I-02-b2")),
            RequirementMatch(canonicalRoomRequirement, MatchStatus.MET, listOf("I-01-b2")),
            RequirementMatch(canonicalGitRequirement, MatchStatus.MET, listOf("I-01-b3")),
        ),
        keywordCoverage = KeywordCoverage(covered = 4, total = 4),
    )

    private fun CoverLetterDraft.asText(): String = listOf(greeting, openingParagraph, evidenceParagraph, closingParagraph)
        .joinToString(" ")
}
