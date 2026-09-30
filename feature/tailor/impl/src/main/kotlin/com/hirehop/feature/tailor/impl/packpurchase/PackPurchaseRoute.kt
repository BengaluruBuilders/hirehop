package com.hirehop.feature.tailor.impl.packpurchase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.api.navigation.PackPurchaseNavKey

@Composable
internal fun PackPurchaseRoute(
    key: PackPurchaseNavKey,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PackPurchaseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) {
        viewModel.toActions(
            onNavigateBack = onNavigateBack,
            onNotNow = onNavigateBack,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    PackPurchaseScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun PackPurchaseViewModel.toActions(
    onNavigateBack: () -> Unit,
    onNotNow: () -> Unit,
): PackPurchaseActions = PackPurchaseActions(
    onSelectPack = { packId -> onAction(PackPurchaseAction.SelectPack(packId)) },
    onBuy = { packId -> onAction(PackPurchaseAction.Buy(packId)) },
    onRestore = { onAction(PackPurchaseAction.Restore) },
    onDismiss = { onAction(PackPurchaseAction.Dismiss) },
    onNotNow = onNotNow,
    onNavigateBack = onNavigateBack,
)
