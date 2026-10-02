package com.hirehop.feature.tailor.impl.exportpreview

import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.impl.document.ExportTemplate

internal sealed interface ExportPreviewAction {
    data class SelectFormat(val format: ExportFormat) : ExportPreviewAction
    data class SelectTemplate(val template: ExportTemplate) : ExportPreviewAction
    data object Export : ExportPreviewAction
    data object RetryPreview : ExportPreviewAction
    data object NavigationHandled : ExportPreviewAction
}

internal data class ExportPreviewActions(
    val onSelectFormat: (ExportFormat) -> Unit,
    val onSelectTemplate: (ExportTemplate) -> Unit,
    val onExport: () -> Unit,
    val onRetry: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onBuyCredits: () -> Unit,
)
