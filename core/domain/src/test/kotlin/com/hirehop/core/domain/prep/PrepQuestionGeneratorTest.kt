package com.hirehop.core.domain.prep

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import com.hirehop.core.testing.data.canonicalAgileRequirement
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.canonicalGapAnalysis
import com.hirehop.core.testing.data.canonicalJobDescription
import com.hirehop.core.testing.data.canonicalKotlinRequirement
import com.hirehop.core.testing.data.canonicalProfileWithoutEntries
import com.hirehop.core.testing.data.canonicalRetrofitRequirement
import org.junit.Test

class PrepQuestionGeneratorTest {
    private val canonical = JobAnalysisResult(canonicalJobDescription, canonicalGapAnalysis)
    private val emptyJob = JobDescription("Engineer", "Northwind", "raw", emptyList())

    @Test
    fun idsAreStableAcrossTwoIdenticalCalls() {
        val first = PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile)
        val second = PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile)

        assertThat(first).isEqualTo(second)
        assertThat(first.map { it.id }).isEqualTo(second.map { it.id })
    }

    @Test
    fun idsComeFromTheRequirementText() {
        val strengths = PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile)
            .filter { it.kind == PrepQuestionKind.STRENGTH }

        assertThat(strengths).isNotEmpty()
        assertThat(strengths.first().id).isEqualTo("prep-strength-strong-kotlin-for-android-app-developmen")
        assertThat(strengths.first().id).doesNotContain(" ")
    }

    @Test
    fun changingTheRequirementTextChangesTheId() {
        val renamed = only(generate(canonicalKotlinRequirement.copy(text = "Kotlin for Android delivery"), MatchStatus.MET, "I-01-b1"))
        val original = only(generate(canonicalKotlinRequirement, MatchStatus.MET, "I-01-b1"))

        assertThat(renamed.id).isNotEqualTo(original.id)
        assertThat(renamed.id).startsWith("prep-strength-kotlin-for-android-deliv")
    }

    @Test
    fun metRequirementBecomesAStrengthQuestionBackedByAProfileFact() {
        val question = only(generate(canonicalKotlinRequirement, MatchStatus.MET, "I-01-b1"))

        assertThat(question.kind).isEqualTo(PrepQuestionKind.STRENGTH)
        assertThat(question.requirementText).isEqualTo(canonicalKotlinRequirement.text)
        assertThat(question.backingFactId).isEqualTo("I-01-b1")
        assertThat(question.prompt).contains("Your record already covers strong Kotlin for Android app development.")
        assertThat(question.prompt).contains("Android developer intern")
    }

    @Test
    fun strengthPromptFallsBackToTheRecordWhenTheEntryHasNoTitle() {
        val untitled = canonicalCandidateProfile.copy(
            entries = listOf(
                ProfileEntry(
                    id = "Z-01",
                    category = EntryCategory.PROJECT,
                    title = "   ",
                    organization = "",
                    startDate = "",
                    endDate = "",
                    bullets = listOf(EvidenceBullet("Z-01-b1", "Shipped a Kotlin Android app to a small team.")),
                    source = FactSource.IMPORTED,
                    isConfirmed = true,
                ),
            ),
        )

        val question = only(generate(canonicalKotlinRequirement, MatchStatus.MET, "Z-01-b1", untitled))

        assertThat(question.kind).isEqualTo(PrepQuestionKind.STRENGTH)
        assertThat(question.prompt).endsWith("Be ready to give one example from your record.")
    }

    @Test
    fun partialRequirementBecomesAClarifyQuestion() {
        val question = only(generate(canonicalRetrofitRequirement, MatchStatus.PARTIAL, "I-02-b2"))

        assertThat(question.kind).isEqualTo(PrepQuestionKind.CLARIFY)
        assertThat(question.backingFactId).isEqualTo("I-02-b2")
        assertThat(question.prompt).contains("Your record covers part of retrofit or Ktor for network calls.")
        assertThat(question.prompt).contains("what you did and what you did not")
    }

    @Test
    fun gapRequirementBecomesAnHonestQuestionWithNoBackingFact() {
        val question = only(generate(canonicalAgileRequirement, MatchStatus.GAP))

        assertThat(question.kind).isEqualTo(PrepQuestionKind.GAP)
        assertThat(question.backingFactId).isNull()
        assertThat(question.prompt).contains("You have no record of it yet.")
        assertThat(question.prompt).contains("say plainly that this part is new for you")
        assertThat(question.prompt).doesNotContain("your experience with")
        assertThat(question.prompt).doesNotContain("describe your work on")
    }

    @Test
    fun gapsComeFirstThenClarifyThenStrength() {
        val questions = PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile, limit = 50)

        assertThat(questions.map { kindOrder(it.kind) }).isInOrder()
        assertThat(questions.filter { it.kind == PrepQuestionKind.GAP }).hasSize(2)
    }

    @Test
    fun everyStrengthAndClarifyQuestionIsBackedByAConfirmedFact() {
        val questions = PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile, limit = 50)

        val held = canonicalCandidateProfile.entries
            .filter { it.isConfirmed }
            .flatMap { entry -> entry.bullets }
            .map { it.id }
            .toSet()

        val backed = questions.filter { it.kind != PrepQuestionKind.GAP }

        assertThat(backed).isNotEmpty()
        assertThat(backed.mapNotNull { it.backingFactId }).isNotEmpty()
        assertThat(backed.map { it.backingFactId }).doesNotContain(null)
        assertThat(backed.count { it.backingFactId in held })
            .isEqualTo(backed.mapNotNull { it.backingFactId }.size)
    }

    @Test
    fun listIsCappedAtTheDefaultMaximum() {
        val questions = PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile)

        assertThat(PrepQuestionGenerator.MAX_QUESTIONS).isEqualTo(6)
        assertThat(questions).hasSize(PrepQuestionGenerator.MAX_QUESTIONS)
    }

    @Test
    fun listHonoursAnExplicitLimit() {
        assertThat(PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile, limit = 2)).hasSize(2)
        assertThat(PrepQuestionGenerator.generate(canonical, canonicalCandidateProfile, limit = 0)).isEmpty()
    }

    @Test
    fun emptyAnalysisYieldsNoQuestions() {
        val empty = JobAnalysisResult(canonicalJobDescription, GapAnalysis(emptyList(), KeywordCoverage(0, 0)))

        assertThat(PrepQuestionGenerator.generate(empty, canonicalCandidateProfile)).isEmpty()
    }

    @Test
    fun nothingIsAskedWhenTheProfileCannotBackAMetRequirement() {
        val questions = PrepQuestionGenerator.generate(canonical, canonicalProfileWithoutEntries, limit = 50)

        assertThat(questions.map { it.kind }.toSet()).containsExactly(PrepQuestionKind.GAP)
    }

    @Test
    fun evidenceIdThatTheProfileDoesNotHoldIsIgnored() {
        assertThat(generate(canonicalKotlinRequirement, MatchStatus.MET, "not-in-the-profile")).isEmpty()
    }

    @Test
    fun unconfirmedEvidenceIsIgnored() {
        val unconfirmed = canonicalCandidateProfile.copy(
            entries = canonicalCandidateProfile.entries.map { entry -> entry.copy(isConfirmed = false) },
        )

        assertThat(generate(canonicalKotlinRequirement, MatchStatus.MET, "I-01-b1", unconfirmed)).isEmpty()
    }

    @Test
    fun requirementWithoutKeywordsIsNotUsedAsEvidence() {
        assertThat(generate(canonicalKotlinRequirement.copy(keywords = emptyList()), MatchStatus.MET, "I-01-b1"))
            .isEmpty()
    }

    @Test
    fun blankRequirementTextIsSkipped() {
        assertThat(generate(canonicalKotlinRequirement.copy(text = "   "), MatchStatus.MET, "I-01-b1")).isEmpty()
    }

    @Test
    fun repeatedRequirementTextStillGetsDistinctStableIds() {
        val analysis = analysisOf(
            listOf(canonicalKotlinRequirement to MatchStatus.MET, canonicalKotlinRequirement to MatchStatus.MET),
            listOf("I-01-b1", "C-02-b1"),
        )

        val first = PrepQuestionGenerator.generate(analysis, canonicalCandidateProfile)
        val second = PrepQuestionGenerator.generate(analysis, canonicalCandidateProfile)

        assertThat(first.map { it.id }).isEqualTo(second.map { it.id })
        assertThat(first[0].id).isEqualTo("prep-strength-strong-kotlin-for-android-app-developmen")
        assertThat(first[1].id).isEqualTo("prep-strength-strong-kotlin-for-android-app-developmen-1")
    }

    @Test
    fun requirementTextWithoutLatinLettersFallsBackToAStableId() {
        val symbol = JobRequirement(
            id = "req-symbol",
            text = "??",
            type = RequirementType.SOFT_SKILL,
            priority = RequirementPriority.NICE_TO_HAVE,
            keywords = listOf("communication"),
        )
        val analysis = analysisOf(listOf(symbol to MatchStatus.GAP, symbol to MatchStatus.GAP), emptyList())

        val questions = PrepQuestionGenerator.generate(analysis, canonicalCandidateProfile)

        assertThat(questions.map { it.id }).containsExactly("prep-gap-requirement", "prep-gap-requirement-1").inOrder()
    }

    private fun kindOrder(kind: PrepQuestionKind): Int = when (kind) {
        PrepQuestionKind.GAP -> 0
        PrepQuestionKind.CLARIFY -> 1
        PrepQuestionKind.STRENGTH -> 2
    }

    private fun only(questions: List<PrepQuestion>): PrepQuestion {
        assertThat(questions).hasSize(1)
        return questions.single()
    }

    private fun generate(
        requirement: JobRequirement,
        status: MatchStatus,
        evidenceId: String? = null,
        profile: CandidateProfile = canonicalCandidateProfile,
    ): List<PrepQuestion> = PrepQuestionGenerator.generate(
        analysisOf(listOf(requirement to status), listOfNotNull(evidenceId)),
        profile,
    )

    private fun analysisOf(
        matches: List<Pair<JobRequirement, MatchStatus>>,
        evidence: List<String>,
    ): JobAnalysisResult {
        val analysisMatches = matches.mapIndexed { index, match ->
            RequirementMatch(
                requirement = match.first,
                status = match.second,
                evidenceIds = evidence.getOrNull(index)?.let { listOf(it) }.orEmpty(),
            )
        }
        return JobAnalysisResult(
            job = emptyJob,
            gap = GapAnalysis(analysisMatches, KeywordCoverage(covered = 0, total = analysisMatches.size)),
        )
    }
}
