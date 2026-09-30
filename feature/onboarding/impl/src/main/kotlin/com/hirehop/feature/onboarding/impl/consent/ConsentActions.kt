package com.hirehop.feature.onboarding.impl.consent

sealed interface ConsentAction {
    data class PurposeToggled(val purpose: ConsentPurpose) : ConsentAction

    data object Agree : ConsentAction

    data object NotNow : ConsentAction

    data object ReadAgain : ConsentAction
}

data class ConsentActions(
    val onPurposeToggle: (ConsentPurpose) -> Unit,
    val onAgree: () -> Unit,
    val onNotNow: () -> Unit,
    val onReadAgain: () -> Unit,
)
