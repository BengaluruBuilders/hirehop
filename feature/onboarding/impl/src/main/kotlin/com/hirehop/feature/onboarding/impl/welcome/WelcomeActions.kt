package com.hirehop.feature.onboarding.impl.welcome

import com.hirehop.core.model.CareerStage

sealed interface WelcomeAction {
    data class CareerStageSelected(val stage: CareerStage) : WelcomeAction

    data object HaveAccountTapped : WelcomeAction

    data object PasteJobDescriptionTapped : WelcomeAction

    data object RetryTapped : WelcomeAction

    data object DismissMessageTapped : WelcomeAction

    data object DestinationConsumed : WelcomeAction
}

data class WelcomeActions(
    val onPasteJobDescription: () -> Unit,
    val onSelectCareerStage: (CareerStage) -> Unit,
    val onHaveAccount: () -> Unit,
    val onRetry: () -> Unit,
    val onDismissMessage: () -> Unit,
)
