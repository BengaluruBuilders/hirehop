package com.hirehop.feature.onboarding.impl.welcome

import com.hirehop.core.model.DebugScenario

enum class WelcomeHeroStage { ORIGINAL, REWRITTEN, THREAD_DRAWN, SETTLED }

enum class WelcomeDestination { PASTE_JOB_DESCRIPTION, IMPORT_RESUME, BUILD_PROFILE_STEP_BY_STEP }

enum class WelcomeMessage { LOAD_FAILED }

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val message: WelcomeMessage? = null,
    val heroStage: WelcomeHeroStage = WelcomeHeroStage.SETTLED,
    val reduceMotion: Boolean = false,
    val destination: WelcomeDestination? = null,
) {
    val isActionsEnabled: Boolean get() = !isLoading
    val showsRewrittenLine: Boolean get() = heroStage != WelcomeHeroStage.ORIGINAL
    val showsProvenanceThread: Boolean get() =
        heroStage == WelcomeHeroStage.THREAD_DRAWN || heroStage == WelcomeHeroStage.SETTLED
    val showsHeroWaitingNote: Boolean get() = heroStage == WelcomeHeroStage.ORIGINAL
}

fun welcomeStateFor(
    scenario: DebugScenario,
    reduceMotion: Boolean = false,
): WelcomeUiState = when (scenario) {
    DebugScenario.LOADING -> WelcomeUiState(isLoading = true, reduceMotion = reduceMotion)

    DebugScenario.OFFLINE -> WelcomeUiState(isOffline = true, reduceMotion = reduceMotion)

    DebugScenario.ERROR -> WelcomeUiState(
        message = WelcomeMessage.LOAD_FAILED,
        reduceMotion = reduceMotion,
    )

    DebugScenario.EMPTY -> WelcomeUiState(
        heroStage = WelcomeHeroStage.ORIGINAL,
        reduceMotion = reduceMotion,
    )

    DebugScenario.PARTIAL -> WelcomeUiState(
        heroStage = WelcomeHeroStage.THREAD_DRAWN,
        reduceMotion = reduceMotion,
    )

    DebugScenario.SUCCESS -> WelcomeUiState(
        heroStage = WelcomeHeroStage.SETTLED,
        reduceMotion = reduceMotion,
    )

    else -> WelcomeUiState(reduceMotion = reduceMotion)
}
