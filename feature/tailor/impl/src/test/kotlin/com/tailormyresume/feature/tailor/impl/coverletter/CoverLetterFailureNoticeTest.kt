package com.tailormyresume.feature.tailor.impl.coverletter

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterDraft
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestCoverLetterRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.CoverLetterNavKey
import com.tailormyresume.feature.tailor.impl.AiNotice
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CoverLetterFailureNoticeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private var failure: AiFailure? = null

    private val source = object : CoverLetterSource {
        override suspend fun invoke(
            candidate: CandidateProfile,
            job: JobDescription,
            analysis: JobAnalysisResult,
            maxEvidence: Int,
        ): CoverLetterDraft = throw AiException(requireNotNull(failure))
    }

    private fun newViewModel() = CoverLetterViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        generateCoverLetter = source,
        connectivityMonitor = TestConnectivityMonitor(),
        contentReportRepository = TestContentReportRepository(),
        coverLetterRepository = TestCoverLetterRepository(),
        exportHistoryRepository = TestExportHistoryRepository(),
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
            viewModel.onEnter(CoverLetterNavKey(canonicalApplication.id, DebugScenario.DEFAULT))
            viewModel.onAction(CoverLetterAction.WriteOne)

            assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.ERROR)
            assertThat(viewModel.uiState.value.failure).isEqualTo(notice)
        }
    }
}
