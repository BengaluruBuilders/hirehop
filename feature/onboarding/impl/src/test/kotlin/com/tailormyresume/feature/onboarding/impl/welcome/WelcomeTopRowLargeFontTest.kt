package com.tailormyresume.feature.onboarding.impl.welcome

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS, fontScale = TmrTestDevices.LARGE_FONT_SCALE)
class WelcomeTopRowLargeFontTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = WelcomeActions(
        onPasteJobDescription = {},
        onSelectCareerStage = {},
        onHaveAccount = {},
        onRetry = {},
        onDismissMessage = {},
    )

    @Test
    fun haveAccountLinkWrapsOnWholeWords() {
        composeRule.setContent {
            TmrTheme { WelcomeScreen(uiState = WelcomeUiState(), actions = actions) }
        }

        val bounds = composeRule.onNodeWithText(LINK, useUnmergedTree = true).getUnclippedBoundsInRoot()

        assertTrue(bounds.width >= LONGEST_WORD_MIN_WIDTH)
        assertTrue(bounds.height <= MAX_TWO_LINE_HEIGHT)
    }

    private companion object {
        const val LINK = "I have an account"
        val LONGEST_WORD_MIN_WIDTH = 64.dp
        val MAX_TWO_LINE_HEIGHT = 72.dp
    }
}
