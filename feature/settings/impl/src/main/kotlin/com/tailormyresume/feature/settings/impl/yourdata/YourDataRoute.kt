package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.settings.api.navigation.YourDataNavKey
import java.io.File

@Composable
internal fun YourDataRoute(
    key: YourDataNavKey,
    onNavigate: (YourDataDestination) -> Unit,
    onBack: () -> Unit,
    onShareFile: (File) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: YourDataViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigate, onBack) {
        YourDataActions(
            onBack = onBack,
            onViewProfile = { onNavigate(YourDataDestination.PROFILE) },
            onCorrectProfile = { onNavigate(YourDataDestination.PROFILE) },
            onViewApplications = { onNavigate(YourDataDestination.APPLICATIONS) },
            onViewPurchases = { onNavigate(YourDataDestination.PURCHASES) },
            onDownload = viewModel::onDownload,
            onDeleteRequest = viewModel::onDeleteRequested,
            onDeleteConfirm = viewModel::onDeleteConfirmed,
            onDeleteDismiss = viewModel::onDeleteDismissed,
            onDeleteMyData = viewModel::onDeleteMyDataRequested,
            onDeleteMyDataConfirm = viewModel::onDeleteMyDataConfirmed,
            onDeleteMyDataDismiss = viewModel::onDeleteMyDataDismissed,
            onCancelExport = viewModel::onCancelExport,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is YourDataEvent.ShareArchive -> onShareFile(event.file)
            }
        }
    }
    YourDataScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
    )
}
