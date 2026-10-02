package com.hirehop.feature.tailor.impl.packpurchase

internal sealed interface PackPurchaseAction {
    data class Buy(val packId: String) : PackPurchaseAction

    data object RetryBuy : PackPurchaseAction

    data object ReloadPacks : PackPurchaseAction

    data object ReturnAfterPurchase : PackPurchaseAction
}

internal data class PackPurchaseActions(
    val onBuy: (String) -> Unit,
    val onRetryBuy: () -> Unit,
    val onReloadPacks: () -> Unit,
    val onNotNow: () -> Unit,
    val onBackToPreview: () -> Unit,
    val onDownloadAfterPurchase: () -> Unit,
    val onOpenCredits: () -> Unit,
    val onNavigateBack: () -> Unit,
)
