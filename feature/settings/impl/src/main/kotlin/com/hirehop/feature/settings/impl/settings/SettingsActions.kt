package com.hirehop.feature.settings.impl.settings

data class SettingsActions(
    val onSignOut: () -> Unit,
    val onSignOutConfirm: () -> Unit,
    val onSignOutDismiss: () -> Unit,
    val onCreditsAndHelp: () -> Unit,
    val onYourData: () -> Unit,
    val onConsentNotice: () -> Unit,
    val onDeleteAccount: () -> Unit,
)
