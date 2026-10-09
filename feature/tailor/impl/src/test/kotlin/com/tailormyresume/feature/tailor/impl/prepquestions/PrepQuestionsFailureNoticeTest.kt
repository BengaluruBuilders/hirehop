package com.tailormyresume.feature.tailor.impl.prepquestions

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.prep.PrepQuestion
import com.tailormyresume.core.domain.prep.PrepQuestionSource
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.tailormyresume.feature.tailor.impl.AiNotice
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PrepQuestionsFailureNoticeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private var failure: AiFailure? = null

    private val source = object : PrepQuestionSource {
        override suspend fun invoke(
            analysis: JobAnalysisResult,
            profile: CandidateProfile,
            limit: Int,
        ): List<PrepQuestion> = throw AiException(requireNotNull(failure))
    }

    private fun newViewModel() = PrepQuestionsViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        generatePrepQuestions = source,
        connectivityMonitor = TestConnectivityMonitor(),
        contentReportRepository = TestContentReportRepository(),
        clock = TestClock(),
    )

    @Test
    fun aBlockedRouteShowsTheMatchingNoticeOnTheErrorCard() = runTest {
        val expected = mapOf(
            AiFailure.RateLimited to AiNotice.RateLimited,
            AiFailure.AnalysisInProgress to AiNotice.InProgress,
            AiFailure.QuotaExceeded to AiNotice.QuotaReached,
            AiFailure.SignInRequired to AiNotice.SignInRequired,
            AiFailure.Unavailable to AiNotice.Generic,
        )
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)

        expected.forEach { (aiFailure, notice) ->
            failure = aiFailure
            val viewModel = newViewModel()
            viewModel.onEnter(PrepQuestionsNavKey(canonicalApplication.id, DebugScenario.DEFAULT))

            assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.ERROR)
            assertThat(viewModel.uiState.value.failure).isEqualTo(notice)
        }
    }
}
