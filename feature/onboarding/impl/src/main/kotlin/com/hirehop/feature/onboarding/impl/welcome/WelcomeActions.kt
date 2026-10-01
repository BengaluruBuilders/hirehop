package com.hirehop.feature.onboarding.impl.welcome

sealed interface WelcomeAction {
    data object PasteJobDescriptionTapped : WelcomeAction

    data object ImportResumeTapped : WelcomeAction

    data object BuildProfileStepByStepTapped : WelcomeAction

    data object RetryTapped : WelcomeAction

    data object DismissMessageTapped : WelcomeAction

    data object DestinationConsumed : WelcomeAction
}

data class WelcomeActions(
    val onPasteJobDescription: () -> Unit,
    val onImportResume: () -> Unit,
    val onBuildProfileStepByStep: () -> Unit,
    val onRetry: () -> Unit,
    val onDismissMessage: () -> Unit,
)
