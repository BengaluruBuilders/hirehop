package com.hirehop.feature.onboarding.impl.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey
import com.hirehop.feature.onboarding.impl.common.observeOffline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val mutableState = MutableStateFlow(WelcomeUiState())

    private var hasEntered = false

    val uiState: StateFlow<WelcomeUiState> = mutableState.asStateFlow()

    fun onEnter(key: WelcomeNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = welcomeStateFor(scenario = key.scenario)
        val forcedOffline = key.scenario == DebugScenario.OFFLINE
        viewModelScope.launch {
            connectivityMonitor.observeOffline(forcedOffline).collect { offline ->
                mutableState.update { it.copy(isOffline = offline) }
            }
        }
    }

    fun onAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.PasteJobDescriptionTapped -> goTo(WelcomeDestination.PASTE_JOB_DESCRIPTION)
            WelcomeAction.ImportResumeTapped -> onImportResume()
            WelcomeAction.BuildProfileStepByStepTapped -> goTo(WelcomeDestination.BUILD_PROFILE_STEP_BY_STEP)
            WelcomeAction.RetryTapped -> mutableState.update { it.copy(message = null) }
            WelcomeAction.DismissMessageTapped -> mutableState.update { it.copy(message = null) }
            WelcomeAction.DestinationConsumed -> mutableState.update { it.copy(destination = null) }
        }
    }

    private fun onImportResume() {
        if (!mutableState.value.isActionsEnabled) return
        viewModelScope.launch {
            val destination = when (nextOnboardingStep()) {
                OnboardingStep.SignIn -> WelcomeDestination.SIGN_IN
                OnboardingStep.Consent -> WelcomeDestination.CONSENT
                else -> WelcomeDestination.IMPORT_RESUME
            }
            goTo(destination)
        }
    }

    private fun goTo(destination: WelcomeDestination) {
        mutableState.update { state ->
            if (state.isActionsEnabled) state.copy(destination = destination) else state
        }
    }
}
