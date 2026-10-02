package com.hirehop.feature.onboarding.impl.signin

sealed interface SignInAction {
    data class AdultConfirmationChanged(val isConfirmed: Boolean) : SignInAction

    data class ReferralCodeChanged(val value: String) : SignInAction

    data object Continue : SignInAction

    data object UnderEighteen : SignInAction

    data object BackFromUnderEighteen : SignInAction

    data object NextStepConsumed : SignInAction
}

data class SignInActions(
    val onAdultConfirmationChange: (Boolean) -> Unit,
    val onReferralCodeChange: (String) -> Unit,
    val onContinue: () -> Unit,
    val onUnderEighteen: () -> Unit,
    val onBackFromUnderEighteen: () -> Unit,
    val onBack: () -> Unit,
)
