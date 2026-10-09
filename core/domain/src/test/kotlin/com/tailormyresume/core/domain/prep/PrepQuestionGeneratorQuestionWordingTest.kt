package com.tailormyresume.core.domain.prep

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalGapAnalysis
import com.tailormyresume.core.testing.data.canonicalJobDescription
import com.tailormyresume.core.testing.data.canonicalKotlinRequirement
import com.tailormyresume.core.testing.data.canonicalRetrofitRequirement
import org.junit.Test

class PrepQuestionGeneratorQuestionWordingTest {

    @Test
    fun strengthPromptIsAQuestionNamingTheRequirementAndTheEntry() {
        val prompt = promptFor(canonicalKotlinRequirement, MatchStatus.MET, "I-01-b1")

        assertThat(prompt).isEqualTo(
            "Walk me through your work on strong Kotlin for Android app development. " +
                "Which example from Android developer intern would you use?",
        )
    }

    @Test
    fun strengthPromptWithoutAnEntryTitleStillAsksAboutTheRecord() {
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

        val prompt = promptFor(canonicalKotlinRequirement, MatchStatus.MET, "Z-01-b1", untitled)

        assertThat(prompt).endsWith("Which example from your record would you use?")
    }

    @Test
    fun clarifyPromptIsAQuestionAboutWhatWasDoneAndWhatIsNew() {
        val prompt = promptFor(canonicalRetrofitRequirement, MatchStatus.PARTIAL, "I-02-b2")

        assertThat(prompt).isEqualTo(
            "Which part of retrofit or Ktor for network calls have you done, and which part is new for you?",
        )
    }

    @Test
    fun everyBackedQuestionEndsWithAQuestionMarkAndIsNotAStatement() {
        val analysis = JobAnalysisResult(canonicalJobDescription, canonicalGapAnalysis)
        val backed = PrepQuestionGenerator.generate(analysis, canonicalCandidateProfile, limit = 50)
            .filter { it.kind != PrepQuestionKind.GAP }

        assertThat(backed).isNotEmpty()
        backed.forEach { question ->
            assertThat(question.prompt).endsWith("?")
            assertThat(question.prompt).doesNotContain("Your record")
            assertThat(question.prompt).doesNotContain("Be ready to")
        }
    }

    @Test
    fun questionsNameOnlyTheBackingEntryTitle() {
        val prompt = promptFor(canonicalKotlinRequirement, MatchStatus.MET, "I-01-b1")

        val otherTitles = canonicalCandidateProfile.entries
            .filter { entry -> entry.bullets.none { it.id == "I-01-b1" } }
            .map { it.title.trim() }
            .filter { it.isNotEmpty() }

        otherTitles.forEach { title -> assertThat(prompt).doesNotContain(title) }
    }

    private fun promptFor(
        requirement: JobRequirement,
        status: MatchStatus,
        evidenceId: String,
        profile: CandidateProfile = canonicalCandidateProfile,
    ): String {
        val match = RequirementMatch(requirement, status, listOf(evidenceId))
        val analysis = JobAnalysisResult(
            job = JobDescription("Engineer", "Northwind", "raw", emptyList()),
            gap = GapAnalysis(listOf(match), KeywordCoverage(covered = 0, total = 1)),
        )
        return PrepQuestionGenerator.generate(analysis, profile).single().prompt
    }
}
