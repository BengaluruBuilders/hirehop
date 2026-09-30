package com.hirehop.feature.tailor.impl.exported

import com.hirehop.core.model.ApplicationStatus

internal sealed interface ExportedAction {
    data object OpenStatusSheet : ExportedAction
    data object DismissStatusSheet : ExportedAction
    data class ConfirmStatus(val status: ApplicationStatus) : ExportedAction
    data object RequestShare : ExportedAction
    data object ShareHandedToSystem : ExportedAction
    data object NavigateBack : ExportedAction
}

internal data class ExportedActions(
    val onOpenStatusSheet: () -> Unit,
    val onDismissStatusSheet: () -> Unit,
    val onConfirmStatus: (ApplicationStatus) -> Unit,
    val onShare: () -> Unit,
    val onNavigateBack: () -> Unit,
)
