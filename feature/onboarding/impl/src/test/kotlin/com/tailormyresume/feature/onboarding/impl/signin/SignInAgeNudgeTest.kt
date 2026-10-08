package com.tailormyresume.feature.onboarding.impl.signin

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInAgeNudgeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()

    private val connectivity = TestConnectivityMonitor()

    private lateinit var gateway: FakeSignInGateway

    private lateinit var viewModel: SignInViewModel

    @Before
    fun setup() {
        gateway = FakeSignInGateway()
        viewModel = newViewModel(gateway)
    }

    private fun newViewModel(signInGateway: SignInGateway): SignInViewModel = SignInViewModel(
        signInGateway = signInGateway,
        nextOnboardingStep = NextOnboardingStepUseCase(session, TestProfileRepository()),
        connectivityMonitor = connectivity,
        sessionRepository = session,
        discardJobDrafts = DiscardJobDraftsUseCase(TestPrepPlanRepository(), TestContentReportRepository()),
    )

    @Test
    fun continue_withoutTheAgeTick_nudgesAndStaysIdle() = runTest {
        viewModel.onEnter(SignInNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.canContinue).isTrue()

        viewModel.onAction(SignInAction.Continue)

        assertThat(viewModel.uiState.value.isAdultNudged).isTrue()
        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IDLE)
        assertThat(gateway.signInCount).isEqualTo(0)
    }

    @Test
    fun continue_afterTheAgeTick_signsInWithoutANudge() = runTest {
        viewModel.onEnter(SignInNavKey(DebugScenario.DEFAULT))
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))

        viewModel.onAction(SignInAction.Continue)

        assertThat(gateway.signInCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.isAdultNudged).isFalse()
    }
}
