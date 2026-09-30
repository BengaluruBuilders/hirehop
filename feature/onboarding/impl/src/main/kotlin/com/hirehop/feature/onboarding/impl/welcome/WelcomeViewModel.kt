package com.hirehop.feature.onboarding.impl.welcome

import androidx.lifecycle.ViewModel
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor() : ViewModel() {

    private val mutableState = MutableStateFlow(WelcomeUiState())

    private var hasEntered = false

    val uiState: StateFlow<WelcomeUiState> = mutableState.asStateFlow()

    fun onEnter(key: WelcomeNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = welcomeStateFor(scenario = key.scenario)
    }

    fun onAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.PasteJobDescriptionTapped -> goTo(WelcomeDestination.PASTE_JOB_DESCRIPTION)
            WelcomeAction.ImportResumeTapped -> goTo(WelcomeDestination.IMPORT_RESUME)
            WelcomeAction.BuildProfileStepByStepTapped -> goTo(WelcomeDestination.BUILD_PROFILE_STEP_BY_STEP)
            WelcomeAction.RetryTapped -> mutableState.update { it.copy(message = null) }
            WelcomeAction.DismissMessageTapped -> mutableState.update { it.copy(message = null) }
            WelcomeAction.DestinationConsumed -> mutableState.update { it.copy(destination = null) }
        }
    }

    private fun goTo(destination: WelcomeDestination) {
        mutableState.update { state ->
            if (state.isActionsEnabled) state.copy(destination = destination) else state
        }
    }
}
