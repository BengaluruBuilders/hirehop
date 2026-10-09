package com.tailormyresume.feature.profile.impl.guidedform

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h2400dp")
class GuidedFormSkillsHintTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun skillsNoteSaysEachSkillBecomesAUserStatedFact() {
        composeRule.setContent {
            TmrTheme { GuidedFormScreen(uiState = GuidedFormUiState(stepIndex = 2), actions = GuidedFormActions.None, onBack = {}) }
        }

        composeRule.onNodeWithText("Each skill becomes a user-stated fact.").assertIsDisplayed()
    }
}
