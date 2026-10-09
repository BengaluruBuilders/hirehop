package com.tailormyresume.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactUseCase
import com.tailormyresume.core.domain.AnalyzeJobUseCase
import com.tailormyresume.core.domain.CreateApplicationUseCase
import com.tailormyresume.core.domain.TailorResumeUseCase
import com.tailormyresume.core.domain.offline.OfflineJobAnalysisSource
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.gateway.TestSignInGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.repository.TestUsageAllowance
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class AnalysisPrefillLabelsTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val sessionRepository = TestSessionRepository()
    private val profileRepository = TestProfileRepository()
    private val matcher = KeywordGapMatcher()
    private lateinit var viewModel: AnalysisViewModel

    private suspend fun TestScope.start(job: KeptJobDescription) {
        sessionRepository.sendAccount(SignInAccount.localAccount)
        sessionRepository.sendConsent(
            ConsentRecord(setOf(ConsentPurpose.READ_AND_BUILD), FixedNow.now(), ConsentRecord.CURRENT_NOTICE_VERSION),
        )
        sessionRepository.sendOnboardingComplete(false)
        profileRepository.sendProfile(confirmedProfile())
        sessionRepository.keepJobDescription(job)
        viewModel = AnalysisViewModel(
            sessionRepository = sessionRepository,
            profileRepository = profileRepository,
            nextOnboardingStep = NextOnboardingStepUseCase(sessionRepository, profileRepository),
            analyzeJob = AnalyzeJobUseCase(OfflineJobAnalysisSource(FixedJobDescriptionAnalyzer(), matcher)),
            gapMatcher = matcher,
            addUserStatedFact = AddUserStatedFactUseCase(profileRepository) { "id" },
            createApplication = CreateApplicationUseCase(
                applicationRepository = TestApplicationRepository(),
                tailorResume = TailorResumeUseCase(EmptyResumeTailor(), AcceptingFabricationGuard()),
                clock = FixedNow,
                idGenerator = { "id" },
            ),
            prepPlanRepository = TestPrepPlanRepository(),
            contentReportRepository = TestContentReportRepository(),
            usageAllowance = TestUsageAllowance(TestClock()),
            paymentGateway = TestPaymentGateway(),
            signInGateway = TestSignInGateway(sessionRepository),
            clock = FixedNow,
            idGenerator = { "id" },
            savedState = SavedStateHandle(),
            connectivityMonitor = TestConnectivityMonitor(),
            computeDispatcher = UnconfinedTestDispatcher(),
            applicationScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()),
        )
        viewModel.onEnter(DebugScenario.DEFAULT)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }
    }

    private fun label() = (viewModel.uiState.value as AnalysisUiState.Result).job

    @Test
    fun untouchedPrefillYieldsToTheBackendLabel() = runTest {
        start(KEPT.copy(roleIsPrefill = true, companyIsPrefill = true))

        assertThat(label()).isEqualTo(JobLabel(title = "Android Developer", company = "Acme"))
    }

    @Test
    fun typedLabelsStillWinOverTheBackend() = runTest {
        start(KEPT)

        assertThat(label()).isEqualTo(JobLabel(title = "Kept Role", company = "Kept Co"))
    }

    @Test
    fun aTypedCompanyWinsWhileAnUntouchedRoleYields() = runTest {
        start(KEPT.copy(roleIsPrefill = true))

        assertThat(label()).isEqualTo(JobLabel(title = "Android Developer", company = "Kept Co"))
    }

    private object FixedNow : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }

    private companion object {
        val KEPT = KeptJobDescription(text = TEST_JOB_TEXT, company = "Kept Co", role = "Kept Role")
    }
}
