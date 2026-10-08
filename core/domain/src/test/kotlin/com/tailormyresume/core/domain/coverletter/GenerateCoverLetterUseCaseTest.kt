package com.tailormyresume.core.domain.coverletter

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalCoroutineRequirement
import com.tailormyresume.core.testing.data.canonicalGapAnalysis
import com.tailormyresume.core.testing.data.canonicalGitRequirement
import com.tailormyresume.core.testing.data.canonicalJobDescription
import com.tailormyresume.core.testing.data.canonicalKotlinRequirement
import com.tailormyresume.core.testing.data.canonicalProfileWithoutEntries
import com.tailormyresume.core.testing.data.canonicalRoomRequirement
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GenerateCoverLetterUseCaseTest {
    private val useCase = GenerateCoverLetterUseCase()
    private val analysis = JobAnalysisResult(canonicalJobDescription, canonicalGapAnalysis)

    @Test
    fun returnsTheComposedDraft() = runTest {
        val draft = useCase(canonicalCandidateProfile, canonicalJobDescription, analysis)

        assertThat(draft).isEqualTo(
            CoverLetterComposer.compose(canonicalCandidateProfile, canonicalJobDescription, analysis),
        )
        assertThat(draft.openingParagraph).contains("Northwind GCC")
    }

    @Test
    fun defaultsToThreeCitedFacts() = runTest {
        val draft = useCase(canonicalCandidateProfile, canonicalJobDescription, wideAnalysis())

        val facts = draft.evidenceParagraph.removePrefix("Here is the work I can show, taken from my own record.")
        assertThat(facts.split("; ")).hasSize(CoverLetterComposer.DEFAULT_MAX_EVIDENCE)
    }

    @Test
    fun honoursTheRequestedEvidenceBudget() = runTest {
        val draft = useCase(canonicalCandidateProfile, canonicalJobDescription, wideAnalysis(), 1)

        val facts = draft.evidenceParagraph.removePrefix("Here is the work I can show, taken from my own record.")
        assertThat(facts.split("; ")).hasSize(1)
    }

    @Test
    fun staysHonestWhenTheProfileHasNoEvidence() = runTest {
        val draft = useCase(canonicalProfileWithoutEntries, canonicalJobDescription, analysis)

        assertThat(draft.evidenceParagraph).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
    }

    private fun wideAnalysis(): JobAnalysisResult = JobAnalysisResult(
        job = canonicalJobDescription,
        gap = GapAnalysis(
            matches = listOf(
                RequirementMatch(canonicalKotlinRequirement, MatchStatus.MET, listOf("I-01-b1")),
                RequirementMatch(canonicalCoroutineRequirement, MatchStatus.MET, listOf("I-02-b2")),
                RequirementMatch(canonicalRoomRequirement, MatchStatus.MET, listOf("I-01-b2")),
                RequirementMatch(canonicalGitRequirement, MatchStatus.MET, listOf("I-01-b3")),
            ),
            keywordCoverage = KeywordCoverage(covered = 4, total = 4),
        ),
    )
}
