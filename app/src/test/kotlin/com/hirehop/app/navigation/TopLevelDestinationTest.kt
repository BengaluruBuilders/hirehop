package com.hirehop.app.navigation

import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.analysis.api.navigation.DefaultAnalysisNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.hirehop.feature.onboarding.api.navigation.DefaultWelcomeNavKey
import com.hirehop.feature.profile.api.navigation.DefaultProfileNavKey
import com.hirehop.feature.settings.api.navigation.DefaultSettingsNavKey
import com.hirehop.feature.settings.api.navigation.YourDataNavKey
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun theThreeTabKeysAreTopLevel() {
        listOf<NavKey>(DefaultApplicationsNavKey, DefaultProfileNavKey, DefaultSettingsNavKey)
            .forEach { key -> assertThat(key.isTopLevelDestination()).isTrue() }
    }

    @Test
    fun aTabKeyWithAScenarioIsStillTopLevel() {
        assertThat(ApplicationsNavKey(scenario = DebugScenario.EMPTY).isTopLevelDestination()).isTrue()
    }

    @Test
    fun pushedAndFirstRunKeysAreNotTopLevel() {
        listOf<NavKey>(
            DefaultWelcomeNavKey,
            DefaultAnalysisNavKey,
            ApplicationDetailNavKey(applicationId = "a-1"),
            YourDataNavKey(),
        ).forEach { key -> assertThat(key.isTopLevelDestination()).isFalse() }
    }
}
