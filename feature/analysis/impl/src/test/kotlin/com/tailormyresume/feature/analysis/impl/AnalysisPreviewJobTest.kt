package com.tailormyresume.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.common.jobs.TrackedJobs
import com.tailormyresume.core.data.repository.UsageAllowance
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
import com.tailormyresume.core.navigation.PendingNavigation
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AnalysisPreviewJobTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private val profiles = TestProfileRepository()
    private val applications = TestApplicationRepository()
    private val clock = TestClock()
    private val matcher = KeywordGapMatcher()
    private val trackedJobs = TrackedJobs()
    private val walletAnswer = CompletableDeferred<Unit>()
    private val applicationScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
    private val waitingAllowance = object : UsageAllowance by TestUsageAllowance(clock) {
        override suspend fun consumeFreeTailoring(): Boolean {
            walletAnswer.await()
            return true
        }
    }

    @Before
    fun setUp() {
        PendingNavigation.consume()
    }

    @After
    fun tearDown() = applicationScope.cancel()

    private fun createViewModel() = AnalysisViewModel(
        sessionRepository = session,
        profileRepository = profiles,
        nextOnboardingStep = NextOnboardingStepUseCase(session, profiles),
        analyzeJob = AnalyzeJobUseCase(OfflineJobAnalysisSource(FixedJobDescriptionAnalyzer(), matcher)),
        gapMatcher = matcher,
        addUserStatedFact = AddUserStatedFactUseCase(profiles) { "id" },
        createApplication = CreateApplicationUseCase(
            applicationRepository = applications,
            tailorResume = TailorResumeUseCase(EmptyResumeTailor(), AcceptingFabricationGuard()),
            clock = clock,
            idGenerator = { "id" },
        ),
        prepPlanRepository = TestPrepPlanRepository(),
        contentReportRepository = TestContentReportRepository(),
        usageAllowance = waitingAllowance,
        paymentGateway = TestPaymentGateway().withFreeCredits(0),
        signInGateway = TestSignInGateway(session),
        clock = clock,
        idGenerator = { "id" },
        savedState = SavedStateHandle(),
        connectivityMonitor = TestConnectivityMonitor(),
        computeDispatcher = UnconfinedTestDispatcher(),
        applicationScope = applicationScope,
        trackedJobs = trackedJobs,
    )

    private suspend fun TestScope.readyViewModel(): AnalysisViewModel {
        session.sendAccount(SignInAccount.localAccount)
        session.sendConsent(ConsentRecord(setOf(ConsentPurpose.READ_AND_BUILD), clock.now(), ConsentRecord.CURRENT_NOTICE_VERSION))
        session.sendOnboardingComplete(true)
        profiles.sendProfile(confirmedProfile())
        session.keepJobDescription(KeptJobDescription(text = TEST_JOB_TEXT, company = "Northwind GCC", role = "Associate Analyst"))
        val viewModel = createViewModel()
        viewModel.onEnter(DebugScenario.DEFAULT)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun aTailoringJobStartedInAPreviewNeverCreatesTheApplicationAfterThePreviewEnds() = runTest {
        val viewModel = readyViewModel()
        trackedJobs.startTracking()
        viewModel.onTailor()

        trackedJobs.cancelTracked()
        walletAnswer.complete(Unit)

        assertThat(applications.observeApplications().first()).isEmpty()
    }

    @Test
    fun aTailoringJobStartedOutsideAPreviewFinishesWhateverHappensToPreviews() = runTest {
        val viewModel = readyViewModel()
        viewModel.onTailor()

        trackedJobs.startTracking()
        trackedJobs.cancelTracked()
        walletAnswer.complete(Unit)

        assertThat(applications.observeApplications().first()).hasSize(1)
    }
}
