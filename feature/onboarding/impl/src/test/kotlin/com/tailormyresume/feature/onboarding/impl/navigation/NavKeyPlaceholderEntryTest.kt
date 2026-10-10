package com.tailormyresume.feature.onboarding.impl.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey
import com.tailormyresume.feature.onboarding.api.navigation.WelcomeNavKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavKeyPlaceholderEntryTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun welcomeNavKeyShowsItsName() {
        composeRule.setContent { NavKeyPlaceholder(WelcomeNavKey()) }

        composeRule.onNodeWithText("WelcomeNavKey").assertIsDisplayed()
    }

    @Test
    fun consentNavKeyShowsItsName() {
        composeRule.setContent { NavKeyPlaceholder(ConsentNavKey()) }

        composeRule.onNodeWithText("ConsentNavKey").assertIsDisplayed()
    }
}
