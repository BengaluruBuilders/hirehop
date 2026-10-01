package com.hirehop.feature.settings.impl.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.settings.api.navigation.SettingsNavKey

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
    val actions = remember(viewModel) { viewModel.toActions() }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.destination) {
        val destination = uiState.destination
        if (destination != null) {
            onNavigate(destination)
            viewModel.onAction(SettingsAction.DestinationConsumed)
        }
    }
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

private fun SettingsViewModel.toActions(): SettingsActions = SettingsActions(
    onRowClick = { row ->
        val destination = row.destination
        if (destination != null) {
            onAction(SettingsAction.DestinationSelected(destination))
        }
    },
)
