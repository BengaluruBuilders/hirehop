package com.hirehop.feature.settings.impl.deleteaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.settings.api.navigation.DeleteAccountNavKey

@Composable
internal fun DeleteAccountRoute(
    key: DeleteAccountNavKey,
    onNavigateBack: () -> Unit,
    onNavigateToYourData: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions(onBack = onNavigateBack) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            DeleteAccountDestination.YOUR_DATA -> {
                viewModel.onAction(DeleteAccountAction.DestinationConsumed)
                onNavigateToYourData()
            }

            DeleteAccountDestination.WELCOME -> {
                viewModel.onAction(DeleteAccountAction.DestinationConsumed)
                onNavigateToWelcome()
            }

            null -> Unit
        }
    }
    DeleteAccountScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
    )
}

private fun DeleteAccountViewModel.toActions(onBack: () -> Unit): DeleteAccountActions = DeleteAccountActions(
    onBack = onBack,
    onKeepAccount = { onAction(DeleteAccountAction.KeepAccountTapped) },
    onDeleteAccount = { onAction(DeleteAccountAction.DeleteAccountTapped) },
    onDownloadData = { onAction(DeleteAccountAction.DownloadDataTapped) },
    onBackToWelcome = { onAction(DeleteAccountAction.BackToWelcomeTapped) },
)
