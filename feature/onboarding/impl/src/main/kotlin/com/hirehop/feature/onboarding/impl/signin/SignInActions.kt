package com.hirehop.feature.onboarding.impl.signin

sealed interface SignInAction {
    data class AdultConfirmationChanged(val isConfirmed: Boolean) : SignInAction

    data object Continue : SignInAction

    data object NotNow : SignInAction

    data object Revisit : SignInAction

    data object UnderEighteen : SignInAction

    data object BackFromUnderEighteen : SignInAction
}

data class SignInActions(
    val onAdultConfirmationChange: (Boolean) -> Unit,
    val onContinue: () -> Unit,
    val onNotNow: () -> Unit,
    val onRevisit: () -> Unit,
    val onUnderEighteen: () -> Unit,
    val onBackFromUnderEighteen: () -> Unit,
)
