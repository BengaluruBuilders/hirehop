package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey

@Composable
internal fun ExportPreviewRoute(
    key: ExportPreviewNavKey,
    onNavigateBack: () -> Unit,
    onExported: (ExportFormat, Boolean) -> Unit,
    onBuyCredits: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportPreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigateBack, onBuyCredits) {
        viewModel.toActions(
            onNavigateBack = onNavigateBack,
            onBuyCredits = { onBuyCredits(viewModel.uiState.value.format) },
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.navigation) {
        when (val navigation = uiState.navigation) {
            null -> return@LaunchedEffect
            is ExportPreviewNavigation.Exported -> onExported(navigation.format, navigation.spentFreeCredit)
            ExportPreviewNavigation.BuyCredits -> onBuyCredits(uiState.format)
        }
        viewModel.onAction(ExportPreviewAction.NavigationHandled)
    }
    ExportPreviewScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun ExportPreviewViewModel.toActions(
    onNavigateBack: () -> Unit,
    onBuyCredits: () -> Unit,
): ExportPreviewActions = ExportPreviewActions(
    onSelectFormat = { format -> onAction(ExportPreviewAction.SelectFormat(format)) },
    onExport = { onAction(ExportPreviewAction.Export) },
    onRetry = { onAction(ExportPreviewAction.RetryPreview) },
    onNavigateBack = onNavigateBack,
    onBuyCredits = onBuyCredits,
)
