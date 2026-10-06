package com.hirehop.feature.onboarding.impl.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.navigation.NavigationState
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.onboarding.api.navigation.DefaultWelcomeNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import com.hirehop.feature.onboarding.api.navigation.navigateToWelcome
import org.junit.Test

class StartOverNavigationTest {

    @Test
    fun navigateToWelcome_dropsPasteJobDescriptionAndSignInFromTheStack() {
        val subStack = NavBackStack<NavKey>(DefaultWelcomeNavKey)
        val navigator = Navigator(
            NavigationState(
                startKey = DefaultWelcomeNavKey,
                topLevelStack = NavBackStack(DefaultWelcomeNavKey),
                subStacks = mapOf(DefaultWelcomeNavKey to subStack),
            ),
        )
        navigator.navigate(PasteJobDescriptionNavKey())
        navigator.navigate(SignInNavKey())

        navigator.navigateToWelcome()

        assertThat(subStack.toList()).containsExactly(DefaultWelcomeNavKey)
    }
}
