package com.hirehop.feature.tailor.impl.coverletter

sealed interface CoverLetterAction {
    data object WriteOne : CoverLetterAction

    data class BeginEdit(val ordinal: Int) : CoverLetterAction

    data class EditTextChanged(val value: String) : CoverLetterAction

    data object SaveEdit : CoverLetterAction

    data object CancelEdit : CoverLetterAction

    data class ReportInaccurate(val ordinal: Int) : CoverLetterAction

    data object DismissMessage : CoverLetterAction

    data object Retry : CoverLetterAction
}

data class CoverLetterActions(
    val onWriteOne: () -> Unit,
    val onBeginEdit: (Int) -> Unit,
    val onEditTextChanged: (String) -> Unit,
    val onSaveEdit: () -> Unit,
    val onCancelEdit: () -> Unit,
    val onReportInaccurate: (Int) -> Unit,
    val onDismissMessage: () -> Unit,
    val onRetry: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onSkipLetter: () -> Unit,
    val onPreviewExport: () -> Unit,
    val onPrepQuestions: () -> Unit,
)
