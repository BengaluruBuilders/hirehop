package com.hirehop.app.navigation

import com.google.common.truth.Truth.assertThat
import com.hirehop.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.hirehop.feature.profile.api.navigation.DefaultProfileNavKey
import org.junit.Test

class TopLevelNavItemTest {

    @Test
    fun startNavKey_isApplications() {
        assertThat(START_NAV_KEY).isEqualTo(DefaultApplicationsNavKey)
    }

    @Test
    fun topLevelNavItems_areApplicationsThenProfile() {
        assertThat(TOP_LEVEL_NAV_ITEMS.keys)
            .containsExactly(DefaultApplicationsNavKey, DefaultProfileNavKey)
            .inOrder()
    }

    @Test
    fun startNavKey_isATopLevelNavKey() {
        assertThat(TOP_LEVEL_NAV_ITEMS).containsKey(START_NAV_KEY)
    }
}
