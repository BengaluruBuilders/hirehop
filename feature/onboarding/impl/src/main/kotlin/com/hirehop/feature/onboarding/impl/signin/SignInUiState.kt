package com.hirehop.feature.onboarding.impl.signin

import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.DebugScenario

enum class SignInStage { IDLE, IN_PROGRESS, SIGNED_IN, CANCELLED, FAILED, UNDER_18 }

data class SignInUiState(
    val stage: SignInStage = SignInStage.IDLE,
    val isAdultConfirmed: Boolean = false,
    val isAdultNudged: Boolean = false,
    val isOffline: Boolean = false,
    val referralCode: String = "",
    val failure: SignInFailureReason? = null,
    val displayName: String? = null,
    val nextStep: OnboardingStep? = null,
) {
    val isBusy: Boolean get() = stage == SignInStage.IN_PROGRESS
    val needsAdultConfirmation: Boolean get() = stage == SignInStage.IDLE && !isAdultConfirmed
    val canContinue: Boolean
        get() = stage != SignInStage.IN_PROGRESS && stage != SignInStage.UNDER_18 && isAdultConfirmed && !isOffline
    val isSettled: Boolean get() = stage == SignInStage.SIGNED_IN
}

fun signInStateFor(scenario: DebugScenario): SignInUiState = when (scenario) {
    DebugScenario.LOADING -> SignInUiState(
        stage = SignInStage.IN_PROGRESS,
        isAdultConfirmed = true,
    )

    DebugScenario.OFFLINE -> SignInUiState(isOffline = true, isAdultConfirmed = true)

    DebugScenario.ERROR -> SignInUiState(
        stage = SignInStage.FAILED,
        isAdultConfirmed = true,
        failure = SignInFailureReason.ProviderUnavailable,
    )

    DebugScenario.PARTIAL -> SignInUiState(
        stage = SignInStage.FAILED,
        isAdultConfirmed = true,
        failure = SignInFailureReason.NetworkUnavailable,
    )

    DebugScenario.SUCCESS -> SignInUiState(
        stage = SignInStage.SIGNED_IN,
        isAdultConfirmed = true,
        displayName = SignInAccount.localAccount.displayName,
    )

    else -> SignInUiState()
}
