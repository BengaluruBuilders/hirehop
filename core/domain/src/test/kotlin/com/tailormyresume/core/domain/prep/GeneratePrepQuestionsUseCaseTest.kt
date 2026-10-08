package com.tailormyresume.core.domain.prep

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalGapAnalysis
import com.tailormyresume.core.testing.data.canonicalJobDescription
import com.tailormyresume.core.testing.data.canonicalProfileWithoutEntries
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GeneratePrepQuestionsUseCaseTest {
    private val useCase = GeneratePrepQuestionsUseCase()
    private val analysis = JobAnalysisResult(canonicalJobDescription, canonicalGapAnalysis)

    @Test
    fun returnsTheGeneratedQuestions() = runTest {
        val questions = useCase(analysis, canonicalCandidateProfile)

        assertThat(questions).isEqualTo(PrepQuestionGenerator.generate(analysis, canonicalCandidateProfile))
        assertThat(questions).isNotEmpty()
    }

    @Test
    fun defaultsToTheGeneratorMaximum() = runTest {
        val questions = useCase(analysis, canonicalCandidateProfile)

        assertThat(questions).hasSize(PrepQuestionGenerator.MAX_QUESTIONS)
    }

    @Test
    fun honoursAnExplicitLimit() = runTest {
        assertThat(useCase(analysis, canonicalCandidateProfile, limit = 1)).hasSize(1)
        assertThat(useCase(analysis, canonicalCandidateProfile, limit = 0)).isEmpty()
    }

    @Test
    fun asksOnlyAboutGapsWhenTheProfileHasNoEvidence() = runTest {
        val questions = useCase(analysis, canonicalProfileWithoutEntries, limit = 50)

        assertThat(questions.map { it.kind }.toSet()).containsExactly(PrepQuestionKind.GAP)
    }

    @Test
    fun repeatedCallsReturnTheSameIds() = runTest {
        val first = useCase(analysis, canonicalCandidateProfile).map { it.id }
        val second = useCase(analysis, canonicalCandidateProfile).map { it.id }

        assertThat(first).isEqualTo(second)
    }
}
