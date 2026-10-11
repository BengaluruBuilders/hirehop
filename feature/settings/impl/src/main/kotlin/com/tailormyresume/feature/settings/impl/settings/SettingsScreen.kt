package com.tailormyresume.feature.settings.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal class SettingsActions(
    val onCredits: () -> Unit,
    val onPageSize: () -> Unit,
    val onFileName: () -> Unit,
    val onProductUpdates: () -> Unit,
    val onDownloadData: () -> Unit,
    val onHelp: () -> Unit,
    val onDeleteAccount: () -> Unit,
    val onSignOut: () -> Unit,
)

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    versionName: String,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) = Unit
