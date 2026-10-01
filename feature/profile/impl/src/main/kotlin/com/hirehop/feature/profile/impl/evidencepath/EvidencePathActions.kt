package com.hirehop.feature.profile.impl.evidencepath

sealed interface EvidencePathAction {
    data class CategoryChosen(
        val category: EvidenceCategory,
    ) : EvidencePathAction

    data class AnswerChanged(
        val prompt: EvidencePrompt,
        val value: String,
    ) : EvidencePathAction

    data object NextPrompt : EvidencePathAction

    data object BackPrompt : EvidencePathAction

    data object SkipPrompt : EvidencePathAction

    data object Save : EvidencePathAction

    data object SkipCategory : EvidencePathAction

    data object AddMore : EvidencePathAction

    data object Finish : EvidencePathAction

    data object DismissMessage : EvidencePathAction
}

data class EvidencePathActions(
    val onCategoryChosen: (EvidenceCategory) -> Unit,
    val onAnswerChanged: (EvidencePrompt, String) -> Unit,
    val onNextPrompt: () -> Unit,
    val onBackPrompt: () -> Unit,
    val onSkipPrompt: () -> Unit,
    val onSave: () -> Unit,
    val onSkipCategory: () -> Unit,
    val onAddMore: () -> Unit,
    val onGoToProfile: () -> Unit,
    val onDismissMessage: () -> Unit,
)
