package com.hirehop.feature.onboarding.impl.signin

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
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
