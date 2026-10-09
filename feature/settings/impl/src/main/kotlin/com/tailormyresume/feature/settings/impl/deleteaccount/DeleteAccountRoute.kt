package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey

@Composable
internal fun DeleteAccountRoute(
    key: DeleteAccountNavKey,
    onNavigateBack: () -> Unit,
    onNavigateToYourData: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigateBack, onNavigateToYourData) {
        DeleteAccountActions(
            onBack = onNavigateBack,
            onKeepAccount = onNavigateBack,
            onDeleteAccount = viewModel::onDeleteTapped,
            onDeleteConfirmed = viewModel::onDeleteConfirmed,
            onDeleteDismissed = viewModel::onDeleteDismissed,
            onDownloadData = onNavigateToYourData,
            onFinishRemoval = viewModel::onFinishRemovalTapped,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    BackHandler(enabled = uiState is DeleteAccountUiState.Deleting) {}
    DeleteAccountScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
    )
}
