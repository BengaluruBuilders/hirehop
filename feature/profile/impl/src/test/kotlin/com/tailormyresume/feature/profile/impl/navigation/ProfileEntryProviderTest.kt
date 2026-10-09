package com.tailormyresume.feature.profile.impl.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.navigation.NavigationState
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import org.junit.Test

private object ProfileTab : NavKey

class ProfileEntryProviderTest {

    private val state = NavigationState(
        startKey = ProfileTab,
        topLevelStack = NavBackStack(ProfileTab),
        subStacks = mapOf(ProfileTab to NavBackStack(ProfileTab)),
    )
    private val navigation = profileNavigation(Navigator(state))

    @Test
    fun addEvidence_opensTheEvidencePathThatReturnsToProfile() {
        navigation.onAddEvidence()

        assertThat(state.currentKey).isEqualTo(FactEvidenceNavKey(returnsToProfile = true))
    }

    @Test
    fun answerQuestions_opensTheGuidedFormThatReturnsToProfile() {
        navigation.onBuildStepByStep()

        assertThat(state.currentKey).isEqualTo(GuidedProfileFormNavKey(returnsToProfile = true))
    }
}
