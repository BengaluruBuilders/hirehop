package com.hirehop.feature.analysis.impl.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.navigation.NavigationState
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.AnalysisNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.tailor.api.navigation.TailorNavKey
import org.junit.Test

private object ApplicationsTab : NavKey
private object ProfileTab : NavKey

class AnalysisEntryProviderTest {

    @Test
    fun openTailor_removesPasteJobDescriptionAndGapAnalysis_andBackLeavesTheTab() {
        val tabs = listOf(ApplicationsTab, ProfileTab)
        val state = NavigationState(
            startKey = ApplicationsTab,
            topLevelStack = NavBackStack(ApplicationsTab),
            subStacks = tabs.associateWith { NavBackStack(it) },
        )
        val navigator = Navigator(state)
        navigator.navigate(PasteJobDescriptionNavKey())
        navigator.navigate(AnalysisNavKey())

        navigator.openTailorFromAnalysis("app-1")

        assertThat(state.currentSubStack.toList()).containsExactly(ApplicationsTab, TailorNavKey("app-1")).inOrder()
        navigator.goBack()
        assertThat(state.currentKey).isEqualTo(ApplicationsTab)
    }
}
