package com.hirehop.feature.settings.impl.yourdata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.settings.api.navigation.YourDataNavKey

@Composable
internal fun YourDataRoute(
    key: YourDataNavKey,
    onNavigate: (YourDataDestination) -> Unit,
    onBack: () -> Unit,
    onShareFile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: YourDataViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions(onBack = onBack) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.destination) {
        val destination = uiState.destination
        if (destination == null) return@LaunchedEffect
        val fileName = uiState.exportFileName
        if (destination == YourDataDestination.SHARE_SHEET && fileName != null) {
            onShareFile(fileName)
        } else {
            onNavigate(destination)
        }
        viewModel.onAction(YourDataAction.DestinationConsumed)
    }
    YourDataScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
    )
}

private fun YourDataViewModel.toActions(onBack: () -> Unit): YourDataActions = YourDataActions(
    onBack = onBack,
    onDownload = { onAction(YourDataAction.DownloadTapped) },
    onShare = { onAction(YourDataAction.ShareTapped) },
    onLedgerAction = { kind, action ->
        onAction(YourDataAction.LedgerActionTapped(kind = kind, action = action))
    },
    onDeleteRequested = { applicationId ->
        onAction(YourDataAction.DeleteRequested(applicationId = applicationId))
    },
    onDeleteConfirmed = { onAction(YourDataAction.DeleteConfirmed) },
    onDeleteDismissed = { onAction(YourDataAction.DeleteDismissed) },
)
