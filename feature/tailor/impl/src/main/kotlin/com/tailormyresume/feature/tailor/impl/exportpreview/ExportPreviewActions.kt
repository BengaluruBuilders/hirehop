package com.tailormyresume.feature.tailor.impl.exportpreview

import com.tailormyresume.core.model.ExportFormat

internal sealed interface ExportPreviewAction {
    data class SelectFormat(val format: ExportFormat) : ExportPreviewAction
    data object Export : ExportPreviewAction
    data object RetryPreview : ExportPreviewAction
    data object NavigationHandled : ExportPreviewAction
    data object CancelExport : ExportPreviewAction
}

internal data class ExportPreviewActions(
    val onSelectFormat: (ExportFormat) -> Unit,
    val onExport: () -> Unit,
    val onRetry: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onBuyCredits: () -> Unit,
    val onCancel: () -> Unit = {},
)
