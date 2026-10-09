package com.tailormyresume.feature.settings.impl.navigation

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import com.tailormyresume.feature.settings.impl.settings.SettingsDestination
import org.junit.Test

class SettingsEntryProviderTest {

    @Test
    fun deleteAccountFromTheOfflineSettingsScenarioOpensTheOfflineScreen() {
        assertThat(deleteAccountNavKey(DebugScenario.OFFLINE))
            .isEqualTo(DeleteAccountNavKey(scenario = DebugScenario.OFFLINE))
    }

    @Test
    fun deleteAccountFromAnyOtherSettingsScenarioOpensTheDefaultScreen() {
        assertThat(deleteAccountNavKey(DebugScenario.DEFAULT)).isEqualTo(DeleteAccountNavKey())
        assertThat(deleteAccountNavKey(DebugScenario.ERROR)).isEqualTo(DeleteAccountNavKey())
    }

    @Test
    fun signInDestination_navigatesToSignInNavKey() {
        assertThat(settingsDestinationNavKey(SettingsDestination.SIGN_IN, DebugScenario.DEFAULT)).isEqualTo(SignInNavKey())
    }
}
