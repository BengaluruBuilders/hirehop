package com.tailormyresume.feature.tailor.impl.exported

import com.tailormyresume.core.model.ApplicationStatus

internal sealed interface ExportedAction {
    data object OpenStatusSheet : ExportedAction
    data object DismissStatusSheet : ExportedAction
    data class ConfirmStatus(val status: ApplicationStatus) : ExportedAction
    data object UndoStatus : ExportedAction
    data object DismissUndo : ExportedAction
    data object RequestShare : ExportedAction
    data object RequestOpen : ExportedAction
    data object FileRequestHandled : ExportedAction
    data object OpenUnavailable : ExportedAction
    data object DismissOpenUnavailable : ExportedAction
}

internal data class ExportedActions(
    val onOpenStatusSheet: () -> Unit,
    val onDismissStatusSheet: () -> Unit,
    val onConfirmStatus: (ApplicationStatus) -> Unit,
    val onUndoStatus: () -> Unit,
    val onDismissUndo: () -> Unit,
    val onShare: () -> Unit,
    val onOpen: () -> Unit,
    val onGetPrepQuestions: () -> Unit,
    val onWriteCoverLetter: () -> Unit,
    val onDone: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onDismissOpenUnavailable: () -> Unit = {},
)
