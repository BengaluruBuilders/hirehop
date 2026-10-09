package com.tailormyresume.feature.onboarding.impl.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS, fontScale = TmrTestDevices.LARGE_FONT_SCALE)
class OnboardingStepBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun title_isNotClippedAtFontScale2() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                Box(Modifier.width(360.dp)) {
                    OnboardingStepBar(
                        onBack = {},
                        title = TITLE,
                        modifier = Modifier.testTag(BAR),
                    )
                }
            }
        }
        composeRule.waitForIdle()

        val node = composeRule.onNodeWithText(TITLE).fetchSemanticsNode()
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        val bar = composeRule.onNodeWithTag(BAR).fetchSemanticsNode()
        assertThat(bar.size.height.toFloat()).isAtLeast(results.single().multiParagraph.height)
    }

    private companion object {
        const val BAR = "bar"
        const val TITLE = "Tell us about\nyour resume"
    }
}
