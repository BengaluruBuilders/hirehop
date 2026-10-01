package com.hirehop.feature.profile.impl.guidedform

sealed interface GuidedFormAction {
    data class ValueChanged(
        val field: GuidedField,
        val value: String,
    ) : GuidedFormAction

    data object Next : GuidedFormAction

    data object Back : GuidedFormAction

    data object SaveAndFinishLater : GuidedFormAction

    data object ContinueNow : GuidedFormAction

    data object StartHandoff : GuidedFormAction

    data object HandoffConsumed : GuidedFormAction

    data object DismissMessage : GuidedFormAction
}

data class GuidedFormActions(
    val onValueChange: (GuidedField, String) -> Unit,
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onSaveAndFinishLater: () -> Unit,
    val onContinueNow: () -> Unit,
    val onStartHandoff: () -> Unit,
    val onDismissMessage: () -> Unit,
)
