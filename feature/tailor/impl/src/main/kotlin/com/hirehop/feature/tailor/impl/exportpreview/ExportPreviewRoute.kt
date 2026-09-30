package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey

@Composable
internal fun ExportPreviewRoute(
    key: ExportPreviewNavKey,
    onNavigateBack: () -> Unit,
    onExported: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportPreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigateBack, onExported) {
        viewModel.toActions(onNavigateBack = onNavigateBack, onExported = onExported)
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    ExportPreviewScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun ExportPreviewViewModel.toActions(
    onNavigateBack: () -> Unit,
    onExported: (ExportFormat) -> Unit,
): ExportPreviewActions = ExportPreviewActions(
    onSelectFormat = { format -> onAction(ExportPreviewAction.SelectFormat(format)) },
    onExport = { onAction(ExportPreviewAction.Export) },
    onRetryPreview = { onAction(ExportPreviewAction.RetryPreview) },
    onDismissResult = { onAction(ExportPreviewAction.DismissResult) },
    onNavigateBack = onNavigateBack,
    onExported = onExported,
)
