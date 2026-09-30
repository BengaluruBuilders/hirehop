package com.hirehop.feature.tailor.impl.credits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey

@Composable
internal fun CreditsRoute(
    key: CreditsNavKey,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreditsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) {
        viewModel.toActions(onNavigateBack = onNavigateBack)
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    CreditsScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun CreditsViewModel.toActions(
    onNavigateBack: () -> Unit,
): CreditsActions = CreditsActions(
    onRestore = { onAction(CreditsAction.Restore) },
    onDismiss = { onAction(CreditsAction.Dismiss) },
    onNavigateBack = onNavigateBack,
)
