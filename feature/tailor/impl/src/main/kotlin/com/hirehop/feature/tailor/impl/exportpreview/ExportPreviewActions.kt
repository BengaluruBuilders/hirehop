package com.hirehop.feature.tailor.impl.exportpreview

internal sealed interface ExportPreviewAction {
    data class SelectFormat(val format: ExportFormat) : ExportPreviewAction
    data object Export : ExportPreviewAction
    data object RetryPreview : ExportPreviewAction
    data object DismissResult : ExportPreviewAction
    data object NavigateBack : ExportPreviewAction
    data class OpenExported(val format: ExportFormat) : ExportPreviewAction
}

internal data class ExportPreviewActions(
    val onSelectFormat: (ExportFormat) -> Unit,
    val onExport: () -> Unit,
    val onRetryPreview: () -> Unit,
    val onDismissResult: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onExported: (ExportFormat) -> Unit,
)
