package com.tailormyresume.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.domain.AddUserStatedFactUseCase
import com.tailormyresume.core.domain.AnalyzeJobUseCase
import com.tailormyresume.core.domain.CreateApplicationUseCase
import com.tailormyresume.core.domain.FirebaseUidProvider
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.JobAnalysisSource
import com.tailormyresume.core.domain.TailorResumeUseCase
import com.tailormyresume.core.domain.offline.OfflineJobAnalysisSource
import com.tailormyresume.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.tailormyresume.core.domain.offline.OfflinePaymentGateway
import com.tailormyresume.core.domain.offline.OfflineResumeTailor
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.sample.OfflineSampleDataController
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.gateway.TestSignInGateway
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.repository.TestUsageAllowance
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.core.testing.util.TestIdGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AnalysisForcedScenarioTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private val profiles = TestProfileRepository()
    private val clock = TestClock()
    private val matcher = KeywordGapMatcher()
    private var remoteCalls = 0
    private val countingSource = object : JobAnalysisSource {
        override suspend fun analyse(profile: CandidateProfile, rawJobText: String): JobAnalysisResult {
            remoteCalls++
            return OfflineJobAnalysisSource(FixedJobDescriptionAnalyzer(), matcher).analyse(profile, rawJobText)
        }
    }
    private val sampleData = OfflineSampleDataController(
        store = TestMockStateStore(),
        sessionRepository = session,
        profileRepository = profiles,
        applicationRepository = TestApplicationRepository(),
        exportHistoryRepository = TestExportHistoryRepository(),
        paymentGateway = OfflinePaymentGateway(TestMockStateStore(), NoMockLatency, clock, TestIdGenerator("order")),
        analyzeJob = AnalyzeJobUseCase(OfflineJobAnalysisSource(OfflineJobDescriptionAnalyzer(), matcher)),
        tailorResume = TailorResumeUseCase(OfflineResumeTailor(), AcceptingFabricationGuard()),
        clock = clock,
        firebaseUid = FirebaseUidProvider { null },
    )

    private fun createViewModel() = AnalysisViewModel(
        sessionRepository = session,
        profileRepository = profiles,
        nextOnboardingStep = NextOnboardingStepUseCase(session, profiles),
        analyzeJob = AnalyzeJobUseCase(countingSource),
        gapMatcher = matcher,
        addUserStatedFact = AddUserStatedFactUseCase(profiles) { "id" },
        createApplication = CreateApplicationUseCase(
            applicationRepository = TestApplicationRepository(),
            tailorResume = TailorResumeUseCase(EmptyResumeTailor(), AcceptingFabricationGuard()),
            clock = clock,
            idGenerator = { "id" },
        ),
        prepPlanRepository = TestPrepPlanRepository(),
        contentReportRepository = TestContentReportRepository(),
        usageAllowance = TestUsageAllowance(clock),
        paymentGateway = TestPaymentGateway(),
        signInGateway = TestSignInGateway(session),
        clock = clock,
        idGenerator = { "id" },
        savedState = SavedStateHandle(),
        connectivityMonitor = TestConnectivityMonitor(),
        computeDispatcher = UnconfinedTestDispatcher(),
        applicationScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()),
    )

    @Test
    fun theSampleSignInPlusAForcedScenarioNeverCallsTheAnalysisSource() = runTest {
        sampleData.keepSampleJobDescription()

        for (scenario in listOf(DebugScenario.ERROR, DebugScenario.EMPTY, DebugScenario.LOADING)) {
            createViewModel().onEnter(scenario)
        }

        assertThat(remoteCalls).isEqualTo(0)
    }

    @Test
    fun theSampleSignInPlusTheDefaultScenarioStillAnalyses() = runTest {
        sampleData.keepSampleJobDescription()

        createViewModel().onEnter(DebugScenario.DEFAULT)

        assertThat(remoteCalls).isEqualTo(1)
    }
}
