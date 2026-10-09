package com.tailormyresume.feature.onboarding.impl.consent

import com.tailormyresume.core.model.ConsentPurpose

sealed interface ConsentAction {
    data class PurposeToggled(val purpose: ConsentPurpose) : ConsentAction

    data object Agree : ConsentAction

    data object NotNow : ConsentAction

    data object ReadAgain : ConsentAction

    data object NextStepConsumed : ConsentAction
}

data class ConsentActions(
    val onPurposeToggle: (ConsentPurpose) -> Unit,
    val onAgree: () -> Unit,
    val onNotNow: () -> Unit,
    val onReadAgain: () -> Unit,
    val onBack: () -> Unit,
    val onBackToStart: () -> Unit = {},
)
