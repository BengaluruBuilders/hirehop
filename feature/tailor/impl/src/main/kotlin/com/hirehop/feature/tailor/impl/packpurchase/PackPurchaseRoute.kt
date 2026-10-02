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
    onOpenCredits: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PackPurchaseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigateBack, onOpenCredits) {
        PackPurchaseActions(
            onBuy = { packId -> viewModel.onAction(PackPurchaseAction.Buy(packId)) },
            onRetryBuy = { viewModel.onAction(PackPurchaseAction.RetryBuy) },
            onReloadPacks = { viewModel.onAction(PackPurchaseAction.ReloadPacks) },
            onNotNow = onNavigateBack,
            onBackToPreview = onNavigateBack,
            onDownloadAfterPurchase = {
                viewModel.onAction(PackPurchaseAction.ReturnAfterPurchase)
                onNavigateBack()
            },
            onOpenCredits = onOpenCredits,
            onNavigateBack = onNavigateBack,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    PackPurchaseScreen(uiState = uiState, actions = actions, modifier = modifier)
}
