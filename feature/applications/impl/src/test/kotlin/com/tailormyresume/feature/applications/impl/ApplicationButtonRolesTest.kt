package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationButtonRolesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun creditsActionIsAButton() {
        composeRule.setContent { TmrTheme(darkTheme = false) { CreditsAction(credits = 3, onClick = {}) } }

        composeRule
            .onNodeWithText("3", substring = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun statusChipIsAButton() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                StatusChip(status = ApplicationStatus.APPLIED, label = "Applied", onClick = {})
            }
        }

        composeRule
            .onNodeWithText("Applied")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }
}
