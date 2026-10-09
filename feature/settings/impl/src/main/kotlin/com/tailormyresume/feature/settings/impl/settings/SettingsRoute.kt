package com.tailormyresume.feature.settings.impl.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.settings.api.navigation.SettingsNavKey

@Composable
internal fun SettingsRoute(
    key: SettingsNavKey,
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName = remember(context) { context.appVersionName() }
    val actions = remember(viewModel, onNavigate) {
        SettingsActions(
            onSignIn = { onNavigate(SettingsDestination.SIGN_IN) },
            onSignOut = viewModel::onSignOutRequested,
            onSignOutConfirm = viewModel::onSignOutConfirmed,
            onSignOutDismiss = viewModel::onSignOutDismissed,
            onCreditsAndHelp = { onNavigate(SettingsDestination.CREDITS_AND_HELP) },
            onYourData = { onNavigate(SettingsDestination.YOUR_DATA) },
            onConsentNotice = { onNavigate(SettingsDestination.CONSENT_NOTICE) },
            onDeleteAccount = { onNavigate(SettingsDestination.DELETE_ACCOUNT) },
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    SettingsScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
        versionName = versionName,
    )
}

private fun Context.appVersionName(): String? = runCatching {
    packageManager.getPackageInfo(packageName, 0).versionName
}.getOrNull()
    ?.takeIf { name -> name.isNotBlank() }
