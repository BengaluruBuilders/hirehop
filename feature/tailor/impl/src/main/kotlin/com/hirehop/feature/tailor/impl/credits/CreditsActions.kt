package com.hirehop.feature.tailor.impl.credits

sealed interface CreditsAction {
    data object Restore : CreditsAction

    data object Dismiss : CreditsAction
}

data class CreditsActions(
    val onRestore: () -> Unit,
    val onDismiss: () -> Unit,
    val onNavigateBack: () -> Unit,
)
