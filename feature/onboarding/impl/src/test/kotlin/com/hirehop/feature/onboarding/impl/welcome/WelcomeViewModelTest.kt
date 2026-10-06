package com.hirehop.feature.onboarding.impl.welcome

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.CareerStage
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class WelcomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private val profile = TestProfileRepository()
    private val connectivity = TestConnectivityMonitor()
    private lateinit var viewModel: WelcomeViewModel

    @Before
    fun setup() {
        viewModel = WelcomeViewModel(NextOnboardingStepUseCase(session, profile), connectivity, session)
    }

    @Test
    fun onEnter_beforeAnyEntry_showsTheSettledFirstRunState() {
        assertThat(viewModel.uiState.value.isActionsEnabled).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun onAction_careerStageSelected_storesTheChoiceAndShowsIt() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.CareerStageSelected(CareerStage.ONE_TO_TWO_YEARS_IN))

        assertThat(session.observeCareerStage().first()).isEqualTo(CareerStage.ONE_TO_TWO_YEARS_IN)
        assertThat(viewModel.uiState.value.careerStage).isEqualTo(CareerStage.ONE_TO_TWO_YEARS_IN)
    }

    @Test
    fun onAction_haveAccountTapped_goesToSignIn() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.HaveAccountTapped)

        assertThat(viewModel.uiState.value.destination).isEqualTo(WelcomeDestination.SIGN_IN)
    }

    @Test
    fun onEnter_loading_namesTheRealStepAndHoldsTheActions() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.LOADING))

        val state = viewModel.uiState.first()
        assertThat(state.isLoading).isTrue()
        assertThat(state.isActionsEnabled).isFalse()
    }

    @Test
    fun onEnter_loading_ignoresAnActionTap() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.LOADING))

        viewModel.onAction(WelcomeAction.PasteJobDescriptionTapped)

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun onEnter_offline_flagsOffline() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.OFFLINE))

        val state = viewModel.uiState.first()
        assertThat(state.isOffline).isTrue()
        assertThat(state.isActionsEnabled).isTrue()
        assertThat(state.message).isNull()
    }

    @Test
    fun onEnter_error_reportsAFailedRead() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.ERROR))

        assertThat(viewModel.uiState.first().message).isEqualTo(WelcomeMessage.LOAD_FAILED)
    }

    @Test
    fun onAction_retry_clearsTheFailedRead() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(WelcomeAction.RetryTapped)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun onAction_dismissMessage_clearsTheFailedRead() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(WelcomeAction.DismissMessageTapped)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.OFFLINE))
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun onAction_pasteJobDescription_asksForThePasteStep() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.PasteJobDescriptionTapped)

        assertThat(viewModel.uiState.value.destination)
            .isEqualTo(WelcomeDestination.PASTE_JOB_DESCRIPTION)
    }

    @Test
    fun onAction_importResume_whenSignedOut_asksForSignIn() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.ImportResumeTapped)

        assertThat(viewModel.uiState.value.destination).isEqualTo(WelcomeDestination.SIGN_IN)
    }

    @Test
    fun onAction_importResume_whenSignedInWithoutConsent_asksForConsent() = runTest {
        session.sendAccount(com.hirehop.core.model.SignInAccount.localAccount)
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.ImportResumeTapped)

        assertThat(viewModel.uiState.value.destination).isEqualTo(WelcomeDestination.CONSENT)
    }

    @Test
    fun onAction_importResume_whenConsentIsRecorded_asksForTheImportStep() = runTest {
        session.sendAccount(com.hirehop.core.model.SignInAccount.localAccount)
        session.sendConsent(
            ConsentRecord(
                purposes = com.hirehop.core.model.ConsentPurpose.entries.toSet(),
                acceptedAt = Instant.fromEpochSeconds(0),
                noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
            ),
        )
        profile.sendProfile(sampleProfile.copy(entries = emptyList()))
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.ImportResumeTapped)

        assertThat(viewModel.uiState.value.destination).isEqualTo(WelcomeDestination.IMPORT_RESUME)
    }

    @Test
    fun onEnter_whenTheDeviceGoesOffline_flagsOffline() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun onAction_buildProfileStepByStep_asksForTheGuidedForm() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(WelcomeAction.BuildProfileStepByStepTapped)

        assertThat(viewModel.uiState.value.destination)
            .isEqualTo(WelcomeDestination.BUILD_PROFILE_STEP_BY_STEP)
    }

    @Test
    fun onAction_destinationConsumed_clearsTheDestination() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(WelcomeAction.PasteJobDescriptionTapped)

        viewModel.onAction(WelcomeAction.DestinationConsumed)

        assertThat(viewModel.uiState.value.destination).isNull()
    }
}
