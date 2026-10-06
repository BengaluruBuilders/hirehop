package com.hirehop.feature.onboarding.impl.signin

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.DiscardJobDraftsUseCase
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInOutcome
import com.hirehop.core.domain.SignInResult
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.CareerStage
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.repository.TestContentReportRepository
import com.hirehop.core.testing.repository.TestPrepPlanRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {

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
    fun underEighteen_clearsTheKeptJobPost() = runTest {
        session.keepJobDescription(KeptJobDescription(text = "Analyst role text", company = "", role = ""))
        session.saveCareerStage(CareerStage.entries.first())
        viewModel.onEnter(SignInNavKey(DebugScenario.DEFAULT))

        viewModel.onAction(SignInAction.UnderEighteen)

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.UNDER_18)
        assertThat(session.observeKeptJobDescription().first()).isNull()
        assertThat(session.observeCareerStage().first()).isNull()
    }

    @Test
    fun defaultScenario_startsIdleAndUnticked() {
        viewModel.onEnter(SignInNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IDLE)
        assertThat(viewModel.uiState.value.isAdultConfirmed).isFalse()
        assertThat(viewModel.uiState.value.needsAdultConfirmation).isTrue()
        assertThat(viewModel.uiState.value.canContinue).isTrue()
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() {
        viewModel.onEnter(SignInNavKey(DebugScenario.SUCCESS))

        viewModel.onEnter(SignInNavKey(DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.SIGNED_IN)
    }

    @Test
    fun loadingScenario_isInProgress() {
        viewModel.onEnter(SignInNavKey(DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IN_PROGRESS)
        assertThat(viewModel.uiState.value.isBusy).isTrue()
    }

    @Test
    fun emptyScenario_startsIdle() {
        viewModel.onEnter(SignInNavKey(DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IDLE)
        assertThat(viewModel.uiState.value.needsAdultConfirmation).isTrue()
    }

    @Test
    fun offlineScenario_holdsTheContinueButton() = runTest {
        viewModel.onEnter(SignInNavKey(DebugScenario.OFFLINE))
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.canContinue).isFalse()

        viewModel.onAction(SignInAction.Continue)

        assertThat(gateway.signInCount).isEqualTo(0)
    }

    @Test
    fun whenTheDeviceGoesOffline_continueNeedsAConnection() = runTest {
        viewModel.onEnter(SignInNavKey())
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.canContinue).isFalse()
    }

    @Test
    fun signIn_onSuccess_asksForTheNextOnboardingStep() = runTest {
        viewModel.onEnter(SignInNavKey())
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))
        session.sendAccount(SignInAccount.localAccount)

        viewModel.onAction(SignInAction.Continue)

        assertThat(viewModel.uiState.value.nextStep).isEqualTo(OnboardingStep.Consent)
    }

    @Test
    fun signIn_onFailure_doesNotMoveOn() = runTest {
        val failing = viewModelWith(failureWith(SignInFailureReason.ProviderUnavailable))
        failing.onEnter(SignInNavKey())
        failing.onAction(SignInAction.AdultConfirmationChanged(true))

        failing.onAction(SignInAction.Continue)

        assertThat(failing.uiState.value.nextStep).isNull()
    }

    @Test
    fun nextStepConsumed_clearsTheStep() = runTest {
        viewModel.onEnter(SignInNavKey())
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))
        viewModel.onAction(SignInAction.Continue)

        viewModel.onAction(SignInAction.NextStepConsumed)

        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun errorScenario_reportsProviderUnavailable() {
        viewModel.onEnter(SignInNavKey(DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.FAILED)
        assertThat(viewModel.uiState.value.failure).isEqualTo(SignInFailureReason.ProviderUnavailable)
    }

    @Test
    fun partialScenario_reportsNetworkUnavailable() {
        viewModel.onEnter(SignInNavKey(DebugScenario.PARTIAL))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.FAILED)
        assertThat(viewModel.uiState.value.failure).isEqualTo(SignInFailureReason.NetworkUnavailable)
    }

    @Test
    fun successScenario_isSignedInOnThisPhone() {
        viewModel.onEnter(SignInNavKey(DebugScenario.SUCCESS))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.SIGNED_IN)
        assertThat(viewModel.uiState.value.displayName).isEqualTo(SignInAccount.localAccount.displayName)
    }

    @Test
    fun userStatedScenario_startsIdle() {
        viewModel.onEnter(SignInNavKey(DebugScenario.USER_STATED))

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IDLE)
        assertThat(viewModel.uiState.value.needsAdultConfirmation).isTrue()
    }

    @Test
    fun continue_withoutTheAgeTick_nudgesAndDoesNotCallTheGateway() = runTest {
        viewModel.onEnter(SignInNavKey())
        viewModel.onAction(SignInAction.AdultConfirmationChanged(false))

        viewModel.onAction(SignInAction.Continue)

        assertThat(viewModel.uiState.value.isAdultNudged).isTrue()
        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IDLE)
        assertThat(gateway.signInCount).isEqualTo(0)
    }

    @Test
    fun continue_whenTicked_signsInAndCarriesTheAccountName() = runTest {
        viewModel.onEnter(SignInNavKey())
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))

        viewModel.onAction(SignInAction.Continue)

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.SIGNED_IN)
        assertThat(viewModel.uiState.value.displayName).isEqualTo(SignInAccount.localAccount.displayName)
        assertThat(gateway.signInCount).isEqualTo(1)
    }

    @Test
    fun continue_whileAlreadyInProgress_doesNotCallTheGateway() = runTest {
        viewModel.onEnter(SignInNavKey(DebugScenario.LOADING))

        viewModel.onAction(SignInAction.Continue)

        assertThat(gateway.signInCount).isEqualTo(0)
    }

    @Test
    fun adultTick_clearsAnEarlierNudge() = runTest {
        viewModel.onEnter(SignInNavKey())

        viewModel.onAction(SignInAction.Continue)
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))

        assertThat(viewModel.uiState.value.isAdultNudged).isFalse()
        assertThat(viewModel.uiState.value.canContinue).isTrue()
    }

    @Test
    fun adultTick_clearsAnEarlierFailure() = runTest {
        val failing = newViewModel(FakeSignInGateway(failureWith(SignInFailureReason.ProviderUnavailable)))
        failing.onEnter(SignInNavKey())
        failing.onAction(SignInAction.Continue)

        failing.onAction(SignInAction.AdultConfirmationChanged(false))

        assertThat(failing.uiState.value.failure).isNull()
    }

    @Test
    fun providerFailure_isCarriedAsItsOwnReason() = runTest {
        val failing = viewModelWith(failureWith(SignInFailureReason.ProviderUnavailable))
        failing.onEnter(SignInNavKey())
        failing.onAction(SignInAction.AdultConfirmationChanged(true))

        failing.onAction(SignInAction.Continue)

        assertThat(failing.uiState.value.stage).isEqualTo(SignInStage.FAILED)
        assertThat(failing.uiState.value.failure).isEqualTo(SignInFailureReason.ProviderUnavailable)
        assertThat(failing.uiState.value.displayName).isNull()
    }

    @Test
    fun networkFailure_isCarriedAsItsOwnReason() = runTest {
        val failing = viewModelWith(failureWith(SignInFailureReason.NetworkUnavailable))
        failing.onEnter(SignInNavKey())
        failing.onAction(SignInAction.AdultConfirmationChanged(true))

        failing.onAction(SignInAction.Continue)

        assertThat(failing.uiState.value.stage).isEqualTo(SignInStage.FAILED)
        assertThat(failing.uiState.value.failure).isEqualTo(SignInFailureReason.NetworkUnavailable)
    }

    @Test
    fun everyFailureReason_hasItsOwnState() = runTest {
        val reasons = SignInFailureReason.entries

        assertThat(reasons).hasSize(2)
        reasons.forEach { reason ->
            val failing = viewModelWith(failureWith(reason))
            failing.onEnter(SignInNavKey())
            failing.onAction(SignInAction.AdultConfirmationChanged(true))
            failing.onAction(SignInAction.Continue)
            assertThat(failing.uiState.value.failure).isEqualTo(reason)
        }
    }

    @Test
    fun cancelledSignIn_isNotAnError() = runTest {
        val cancelling = viewModelWith(SignInResult.Cancelled)
        cancelling.onEnter(SignInNavKey())
        cancelling.onAction(SignInAction.AdultConfirmationChanged(true))

        cancelling.onAction(SignInAction.Continue)

        assertThat(cancelling.uiState.value.stage).isEqualTo(SignInStage.CANCELLED)
        assertThat(cancelling.uiState.value.failure).isNull()
    }

    @Test
    fun retryAfterAFailure_reachesTheGatewayAgain() = runTest {
        val failingGateway = FakeSignInGateway(failureWith(SignInFailureReason.ProviderUnavailable))
        val failing = newViewModel(failingGateway)
        failing.onEnter(SignInNavKey())
        failing.onAction(SignInAction.AdultConfirmationChanged(true))

        failing.onAction(SignInAction.Continue)
        assertThat(failingGateway.signInCount).isEqualTo(1)

        failing.onAction(SignInAction.Continue)
        assertThat(failingGateway.signInCount).isEqualTo(2)
    }

    @Test
    fun retryAfterAFailure_signsInWhenTheGatewayRecovers() = runTest {
        val recovering = RecoveringSignInGateway(
            first = SignInResult.Failed(SignInFailureReason.ProviderUnavailable),
            then = SignInResult.SignedIn(SignInAccount.localAccount),
        )
        val viewModelUnderTest = newViewModel(recovering)
        viewModelUnderTest.onEnter(SignInNavKey())
        viewModelUnderTest.onAction(SignInAction.AdultConfirmationChanged(true))
        viewModelUnderTest.onAction(SignInAction.Continue)
        assertThat(viewModelUnderTest.uiState.value.stage).isEqualTo(SignInStage.FAILED)

        viewModelUnderTest.onAction(SignInAction.Continue)

        assertThat(viewModelUnderTest.uiState.value.stage).isEqualTo(SignInStage.SIGNED_IN)
        assertThat(viewModelUnderTest.uiState.value.displayName)
            .isEqualTo(SignInAccount.localAccount.displayName)
    }

    @Test
    fun theShippedOfflineGateway_signsInLocally() = runTest {
        val offline = viewModelWith(offlineGatewayWith(SignInOutcome.SignedIn))
        offline.onEnter(SignInNavKey())
        offline.onAction(SignInAction.AdultConfirmationChanged(true))

        offline.onAction(SignInAction.Continue)

        assertThat(offline.uiState.value.stage).isEqualTo(SignInStage.SIGNED_IN)
        assertThat(offline.uiState.value.displayName).isNotEmpty()
    }

    @Test
    fun theShippedOfflineGateway_canCancel() = runTest {
        val offline = viewModelWith(offlineGatewayWith(SignInOutcome.Cancelled))
        offline.onEnter(SignInNavKey())
        offline.onAction(SignInAction.AdultConfirmationChanged(true))

        offline.onAction(SignInAction.Continue)

        assertThat(offline.uiState.value.stage).isEqualTo(SignInStage.CANCELLED)
    }

    @Test
    fun theShippedOfflineGateway_canFail() = runTest {
        val offline = viewModelWith(offlineGatewayWith(SignInOutcome.Failed))
        offline.onEnter(SignInNavKey())
        offline.onAction(SignInAction.AdultConfirmationChanged(true))

        offline.onAction(SignInAction.Continue)

        assertThat(offline.uiState.value.failure).isEqualTo(SignInFailureReason.ProviderUnavailable)
    }

    @Test
    fun underEighteen_isACalmStopThatSendsNothing() = runTest {
        viewModel.onEnter(SignInNavKey())
        viewModel.onAction(SignInAction.AdultConfirmationChanged(true))

        viewModel.onAction(SignInAction.UnderEighteen)

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.UNDER_18)
        assertThat(gateway.signInCount).isEqualTo(0)

        viewModel.onAction(SignInAction.BackFromUnderEighteen)

        assertThat(viewModel.uiState.value.stage).isEqualTo(SignInStage.IDLE)
    }

    private fun viewModelWith(result: SignInResult): SignInViewModel =
        newViewModel(FakeSignInGateway(result))

    private fun viewModelWith(gateway: SignInGateway): SignInViewModel =
        newViewModel(gateway)
}
