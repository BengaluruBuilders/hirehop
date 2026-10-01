package com.hirehop.feature.settings.impl.deleteaccount

sealed interface DeleteAccountAction {
    data object KeepAccountTapped : DeleteAccountAction

    data object DeleteAccountTapped : DeleteAccountAction

    data object DownloadDataTapped : DeleteAccountAction

    data object BackToWelcomeTapped : DeleteAccountAction

    data object DestinationConsumed : DeleteAccountAction
}

data class DeleteAccountActions(
    val onBack: () -> Unit,
    val onKeepAccount: () -> Unit,
    val onDeleteAccount: () -> Unit,
    val onDownloadData: () -> Unit,
    val onBackToWelcome: () -> Unit,
)
