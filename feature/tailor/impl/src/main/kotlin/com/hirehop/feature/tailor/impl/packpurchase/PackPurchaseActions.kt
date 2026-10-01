package com.hirehop.feature.tailor.impl.packpurchase

sealed interface PackPurchaseAction {
    data class SelectPack(val packId: String) : PackPurchaseAction

    data class Buy(val packId: String) : PackPurchaseAction

    data object Restore : PackPurchaseAction

    data object Dismiss : PackPurchaseAction
}

data class PackPurchaseActions(
    val onSelectPack: (String) -> Unit,
    val onBuy: (String) -> Unit,
    val onRestore: () -> Unit,
    val onDismiss: () -> Unit,
    val onNotNow: () -> Unit,
    val onNavigateBack: () -> Unit,
)
