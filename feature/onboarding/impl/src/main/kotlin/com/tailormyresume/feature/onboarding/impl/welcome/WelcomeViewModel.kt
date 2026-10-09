package com.tailormyresume.feature.onboarding.impl.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.onboarding.api.navigation.WelcomeNavKey
import com.tailormyresume.feature.onboarding.impl.common.observeOffline
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
    private val sessionRepository: SessionRepository,
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
            sessionRepository.observeCareerStage().collect { stage ->
                mutableState.update { it.copy(careerStage = stage) }
            }
        }
        viewModelScope.launch {
            if (nextOnboardingStep() == OnboardingStep.Consent) goTo(WelcomeDestination.CONSENT)
        }
        viewModelScope.launch {
            connectivityMonitor.observeOffline(forcedOffline).collect { offline ->
                mutableState.update { it.copy(isOffline = offline) }
            }
        }
    }

    fun onAction(action: WelcomeAction) {
        when (action) {
            is WelcomeAction.CareerStageSelected -> onCareerStageSelected(action.stage)
            WelcomeAction.HaveAccountTapped -> goTo(WelcomeDestination.SIGN_IN)
            WelcomeAction.PasteJobDescriptionTapped -> goTo(WelcomeDestination.PASTE_JOB_DESCRIPTION)
            WelcomeAction.RetryTapped -> mutableState.update { it.copy(message = null) }
            WelcomeAction.DismissMessageTapped -> mutableState.update { it.copy(message = null) }
            WelcomeAction.DestinationConsumed -> mutableState.update { it.copy(destination = null) }
        }
    }

    private fun onCareerStageSelected(stage: CareerStage) {
        viewModelScope.launch { sessionRepository.saveCareerStage(stage) }
    }

    private fun goTo(destination: WelcomeDestination) {
        mutableState.update { state ->
            if (state.isActionsEnabled) state.copy(destination = destination) else state
        }
    }
}
