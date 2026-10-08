package com.tailormyresume.feature.settings.impl.yourdata

data class YourDataActions(
    val onBack: () -> Unit,
    val onViewProfile: () -> Unit,
    val onCorrectProfile: () -> Unit,
    val onViewApplications: () -> Unit,
    val onViewPurchases: () -> Unit,
    val onDownload: () -> Unit,
    val onDeleteRequest: (String) -> Unit,
    val onDeleteConfirm: () -> Unit,
    val onDeleteDismiss: () -> Unit,
)
