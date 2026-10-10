package com.tailormyresume.feature.profile.impl.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.api.navigation.ProfileNavKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class NavKeyPlaceholderEntryTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileNavKeyRendersKeySimpleName() {
        composeRule.setContent { NavKeyPlaceholder(ProfileNavKey()) }

        composeRule.onNodeWithText("ProfileNavKey").assertIsDisplayed()
    }

    @Test
    fun factEvidenceNavKeyRendersKeySimpleName() {
        composeRule.setContent { NavKeyPlaceholder(FactEvidenceNavKey()) }

        composeRule.onNodeWithText("FactEvidenceNavKey").assertIsDisplayed()
    }
}
