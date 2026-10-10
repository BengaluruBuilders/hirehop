package com.tailormyresume.feature.onboarding.impl.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
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
    fun signInNavKeyShowsItsName() {
        composeRule.setContent { NavKeyPlaceholder(SignInNavKey()) }

        composeRule.onNodeWithText("SignInNavKey").assertIsDisplayed()
    }

    @Test
    fun pasteJobDescriptionNavKeyShowsItsName() {
        composeRule.setContent { NavKeyPlaceholder(PasteJobDescriptionNavKey()) }

        composeRule.onNodeWithText("PasteJobDescriptionNavKey").assertIsDisplayed()
    }
}
