package com.hirehop.feature.settings.impl.yourdata

data class YourDataActions(
    val onBack: () -> Unit = {},
    val onDownload: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onLedgerAction: (YourDataLedgerKind, YourDataLedgerAction) -> Unit = { _, _ -> },
    val onDeleteRequested: (String) -> Unit = {},
    val onDeleteConfirmed: () -> Unit = {},
    val onDeleteDismissed: () -> Unit = {},
)
