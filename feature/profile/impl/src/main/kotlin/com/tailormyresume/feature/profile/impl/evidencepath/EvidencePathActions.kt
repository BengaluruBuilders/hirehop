package com.tailormyresume.feature.profile.impl.evidencepath

sealed interface EvidencePathAction {
    data class CategoryChosen(val category: EvidenceCategory) : EvidencePathAction

    data class AnswerChanged(val value: String) : EvidencePathAction

    data object Save : EvidencePathAction

    data object Skip : EvidencePathAction

    data object AddMore : EvidencePathAction

    data object Finish : EvidencePathAction

    data object NavigationConsumed : EvidencePathAction

    data object DismissMessage : EvidencePathAction
}

data class EvidencePathActions(
    val onCategoryChosen: (EvidenceCategory) -> Unit,
    val onAnswerChanged: (String) -> Unit,
    val onSave: () -> Unit,
    val onSkip: () -> Unit,
    val onAddMore: () -> Unit,
    val onFinish: () -> Unit,
    val onEditFact: (entryId: String, entryType: String) -> Unit,
) {
    companion object {
        val None = EvidencePathActions(
            onCategoryChosen = {},
            onAnswerChanged = {},
            onSave = {},
            onSkip = {},
            onAddMore = {},
            onFinish = {},
            onEditFact = { _, _ -> },
        )
    }
}
