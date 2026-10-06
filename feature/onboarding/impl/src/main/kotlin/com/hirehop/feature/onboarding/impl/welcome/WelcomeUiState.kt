package com.hirehop.feature.onboarding.impl.welcome

import com.hirehop.core.model.CareerStage
import com.hirehop.core.model.DebugScenario

enum class WelcomeDestination { PASTE_JOB_DESCRIPTION, SIGN_IN, CONSENT, IMPORT_RESUME, BUILD_PROFILE_STEP_BY_STEP }

enum class WelcomeMessage { LOAD_FAILED }

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val message: WelcomeMessage? = null,
    val destination: WelcomeDestination? = null,
    val careerStage: CareerStage? = null,
) {
    val isActionsEnabled: Boolean get() = !isLoading
}

fun welcomeStateFor(scenario: DebugScenario): WelcomeUiState = when (scenario) {
    DebugScenario.LOADING -> WelcomeUiState(isLoading = true)

    DebugScenario.OFFLINE -> WelcomeUiState(isOffline = true)

    DebugScenario.ERROR -> WelcomeUiState(message = WelcomeMessage.LOAD_FAILED)

    else -> WelcomeUiState()
}
