package com.hirehop.feature.onboarding.impl.welcome

import com.hirehop.core.model.DebugScenario

enum class WelcomeHeroStage { ORIGINAL, REWRITTEN, THREAD_DRAWN, SETTLED }

enum class WelcomeDestination { PASTE_JOB_DESCRIPTION, SIGN_IN, CONSENT, IMPORT_RESUME, BUILD_PROFILE_STEP_BY_STEP }

enum class WelcomeMessage { LOAD_FAILED }

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val message: WelcomeMessage? = null,
    val heroStage: WelcomeHeroStage = WelcomeHeroStage.SETTLED,
    val destination: WelcomeDestination? = null,
) {
    val isActionsEnabled: Boolean get() = !isLoading
    val showsRewrittenLine: Boolean get() = heroStage != WelcomeHeroStage.ORIGINAL
    val showsProvenanceThread: Boolean get() =
        heroStage == WelcomeHeroStage.THREAD_DRAWN || heroStage == WelcomeHeroStage.SETTLED
    val showsNeverInventsChip: Boolean get() = heroStage == WelcomeHeroStage.SETTLED
}

fun welcomeStateFor(scenario: DebugScenario): WelcomeUiState = when (scenario) {
    DebugScenario.LOADING -> WelcomeUiState(isLoading = true)

    DebugScenario.OFFLINE -> WelcomeUiState(isOffline = true)

    DebugScenario.ERROR -> WelcomeUiState(message = WelcomeMessage.LOAD_FAILED)

    DebugScenario.EMPTY -> WelcomeUiState(heroStage = WelcomeHeroStage.ORIGINAL)

    DebugScenario.PARTIAL -> WelcomeUiState(heroStage = WelcomeHeroStage.THREAD_DRAWN)

    DebugScenario.SUCCESS -> WelcomeUiState(heroStage = WelcomeHeroStage.SETTLED)

    else -> WelcomeUiState()
}
