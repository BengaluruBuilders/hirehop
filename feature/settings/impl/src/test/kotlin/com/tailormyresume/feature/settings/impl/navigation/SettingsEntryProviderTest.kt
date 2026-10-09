package com.tailormyresume.feature.settings.impl.navigation

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
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
}
