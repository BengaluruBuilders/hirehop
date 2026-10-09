package com.tailormyresume.feature.settings.impl.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey
import com.tailormyresume.feature.profile.api.navigation.ProfileNavKey
import com.tailormyresume.feature.settings.api.navigation.AccountDeletedNavKey
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import com.tailormyresume.feature.settings.api.navigation.SettingsNavKey
import com.tailormyresume.feature.settings.api.navigation.YourDataNavKey
import com.tailormyresume.feature.settings.impl.R
import com.tailormyresume.feature.settings.impl.deleteaccount.AccountDeletedRoute
import com.tailormyresume.feature.settings.impl.deleteaccount.DeleteAccountRoute
import com.tailormyresume.feature.settings.impl.settings.SettingsDestination
import com.tailormyresume.feature.settings.impl.settings.SettingsRoute
import com.tailormyresume.feature.settings.impl.yourdata.YourDataDestination
import com.tailormyresume.feature.settings.impl.yourdata.YourDataRoute
import com.tailormyresume.feature.tailor.api.navigation.CreditsNavKey

fun EntryProviderScope<NavKey>.settingsEntry(navigator: Navigator) {
    entry<SettingsNavKey> { key ->
        SettingsRoute(
            key = key,
            onNavigate = { destination -> navigator.navigateToSettingsDestination(destination, key.scenario) },
        )
    }
    entry<YourDataNavKey> { key ->
        val context = LocalContext.current
        val subject = stringResource(R.string.feature_settings_impl_your_data_share_subject)
        val chooserTitle = stringResource(R.string.feature_settings_impl_your_data_share_chooser)
        YourDataRoute(
            key = key,
            onNavigate = { destination -> navigator.navigateToYourDataDestination(destination) },
            onBack = { navigator.goBack() },
            onShareFile = { file ->
                context.startActivity(
                    createSettingsShareIntent(
                        context = context,
                        file = file,
                        subject = subject,
                        chooserTitle = chooserTitle,
                    ),
                )
            },
        )
    }
    entry<AccountDeletedNavKey> {
        AccountDeletedRoute(onDone = { navigator.goBack() })
    }
    entry<DeleteAccountNavKey> { key ->
        DeleteAccountRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onNavigateToYourData = { navigator.navigate(YourDataNavKey()) },
        )
    }
}

private fun Navigator.navigateToSettingsDestination(destination: SettingsDestination, scenario: DebugScenario) {
    when (destination) {
        SettingsDestination.CREDITS_AND_HELP -> navigate(CreditsNavKey())
        SettingsDestination.YOUR_DATA -> navigate(YourDataNavKey())
        SettingsDestination.CONSENT_NOTICE -> navigate(ConsentNavKey(readOnly = true))
        SettingsDestination.DELETE_ACCOUNT -> navigate(deleteAccountNavKey(scenario))
    }
}

internal fun deleteAccountNavKey(settingsScenario: DebugScenario): DeleteAccountNavKey =
    if (settingsScenario == DebugScenario.OFFLINE) DeleteAccountNavKey(scenario = DebugScenario.OFFLINE) else DeleteAccountNavKey()

private fun Navigator.navigateToYourDataDestination(destination: YourDataDestination) {
    when (destination) {
        YourDataDestination.PROFILE -> navigate(ProfileNavKey())
        YourDataDestination.APPLICATIONS -> navigate(DefaultApplicationsNavKey)
        YourDataDestination.PURCHASES -> navigate(CreditsNavKey())
    }
}
