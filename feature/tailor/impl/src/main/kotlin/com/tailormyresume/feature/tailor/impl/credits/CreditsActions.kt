package com.tailormyresume.feature.tailor.impl.credits

internal sealed interface CreditsAction {
    data object Retry : CreditsAction
}

internal data class CreditsActions(
    val onGetPack: () -> Unit,
    val onAskRefund: () -> Unit,
    val onContactHelp: () -> Unit,
    val onRetry: () -> Unit,
    val onNavigateBack: () -> Unit,
)
