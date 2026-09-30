package com.hirehop.feature.settings.impl.navigation

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey
import com.hirehop.feature.profile.api.navigation.ProfileNavKey
import com.hirehop.feature.settings.api.navigation.DeleteAccountNavKey
import com.hirehop.feature.settings.api.navigation.SettingsNavKey
import com.hirehop.feature.settings.api.navigation.YourDataNavKey
import com.hirehop.feature.settings.impl.R
import com.hirehop.feature.settings.impl.deleteaccount.DeleteAccountRoute
import com.hirehop.feature.settings.impl.settings.SettingsDestination
import com.hirehop.feature.settings.impl.settings.SettingsRoute
import com.hirehop.feature.settings.impl.yourdata.YourDataDestination
import com.hirehop.feature.settings.impl.yourdata.YourDataRoute
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface SettingsExportEntryPoint {
    fun settingsDataExportWriter(): SettingsDataExportWriter
}

fun EntryProviderScope<NavKey>.settingsEntry(navigator: Navigator) {
    entry<SettingsNavKey> { key ->
        SettingsRoute(
            key = key,
            onNavigate = { destination -> navigator.navigateToSettingsDestination(destination) },
        )
    }
    entry<YourDataNavKey> { key ->
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val writer = remember(context) { context.settingsDataExportWriter() }
        val subject = stringResource(R.string.feature_settings_impl_your_data_share_subject)
        val chooserTitle = stringResource(R.string.feature_settings_impl_your_data_share_chooser)
        YourDataRoute(
            key = key,
            onNavigate = { destination -> navigator.navigateToYourDataDestination(destination) },
            onBack = { navigator.goBack() },
            onShareFile = { fileName ->
                scope.launch {
                    val file = runCatching { writer.write(fileName) }.getOrNull() ?: return@launch
                    context.startActivity(
                        createSettingsShareIntent(
                            context = context,
                            file = file,
                            subject = subject,
                            chooserTitle = chooserTitle,
                        ),
                    )
                }
            },
        )
    }
    entry<DeleteAccountNavKey> { key ->
        DeleteAccountRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onNavigateToYourData = { navigator.navigate(YourDataNavKey()) },
            onNavigateToWelcome = { navigator.navigate(WelcomeNavKey()) },
        )
    }
}

private fun Context.settingsDataExportWriter(): SettingsDataExportWriter =
    EntryPointAccessors.fromApplication(
        applicationContext,
        SettingsExportEntryPoint::class.java,
    ).settingsDataExportWriter()

private fun Navigator.navigateToSettingsDestination(destination: SettingsDestination) {
    when (destination) {
        SettingsDestination.SIGN_OUT -> navigate(WelcomeNavKey())
        SettingsDestination.CREDITS_AND_HELP -> navigate(CreditsNavKey())
        SettingsDestination.YOUR_DATA -> navigate(YourDataNavKey())
        SettingsDestination.CONSENT_NOTICE -> navigate(ConsentNavKey())
        SettingsDestination.DELETE_ACCOUNT -> navigate(DeleteAccountNavKey())
    }
}

private fun Navigator.navigateToYourDataDestination(destination: YourDataDestination) {
    when (destination) {
        YourDataDestination.PROFILE_VIEW -> navigate(ProfileNavKey())
        YourDataDestination.PROFILE_CORRECT -> navigate(ProfileNavKey())
        YourDataDestination.APPLICATIONS_VIEW -> navigate(DefaultApplicationsNavKey)
        YourDataDestination.PURCHASES_VIEW -> navigate(CreditsNavKey())
        YourDataDestination.SHARE_SHEET -> Unit
    }
}
