package com.tailormyresume.feature.settings.impl.deleteaccount

data class DeleteAccountActions(
    val onBack: () -> Unit,
    val onKeepAccount: () -> Unit,
    val onDeleteAccount: () -> Unit,
    val onDeleteConfirmed: () -> Unit,
    val onDeleteDismissed: () -> Unit,
    val onDownloadData: () -> Unit,
)
