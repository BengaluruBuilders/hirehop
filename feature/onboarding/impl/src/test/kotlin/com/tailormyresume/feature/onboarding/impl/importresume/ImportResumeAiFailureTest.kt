package com.tailormyresume.feature.onboarding.impl.importresume

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.ResumeTextParser
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.ImportResumeNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImportResumeAiFailureTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val file = ResumeFile("Resume_2026.pdf", RESUME_PDF_MIME, 1024L, "content://documents/1")

    private fun importFailingWith(failure: Throwable): ImportResumeUiState {
        val repository = TestProfileRepository().apply { sendProfile(null) }
        val viewModel = ImportResumeViewModel(
            resumeTextSource = object : ResumeTextSource {
                override suspend fun read(file: ResumeFile): ResumeRead = ResumeRead.Text("Data intern")
            },
            resumeTextParser = object : ResumeTextParser {
                override suspend fun parse(rawText: String): CandidateProfile = throw failure
            },
            factIdAllocator = FactIdAllocator(),
            profileRepository = repository,
            connectivityMonitor = TestConnectivityMonitor(),
        ).apply { onEnter(ImportResumeNavKey(scenario = DebugScenario.DEFAULT)) }
        viewModel.onFileChosen(file)
        return viewModel.uiState.value
    }

    @Test
    fun rateLimitedParseShowsTheRateLimitCause() = runTest {
        val state = importFailingWith(AiException(AiFailure.RateLimited))
        assertThat(state.stage).isEqualTo(ImportStage.Failed)
        assertThat(state.failureCause).isEqualTo(ImportFailureCause.RateLimited)
    }

    @Test
    fun quotaParseShowsTheQuotaCause() = runTest {
        assertThat(importFailingWith(AiException(AiFailure.QuotaExceeded)).failureCause)
            .isEqualTo(ImportFailureCause.QuotaReached)
    }

    @Test
    fun signInParseShowsTheSignInCause() = runTest {
        assertThat(importFailingWith(AiException(AiFailure.SignInRequired)).failureCause)
            .isEqualTo(ImportFailureCause.SignInRequired)
    }

    @Test
    fun otherParseFailuresStayGeneric() = runTest {
        assertThat(importFailingWith(AiException(AiFailure.Unavailable)).failureCause)
            .isEqualTo(ImportFailureCause.Generic)
        assertThat(importFailingWith(IllegalStateException("x")).failureCause)
            .isEqualTo(ImportFailureCause.Generic)
    }
}
