package com.tailormyresume.feature.settings.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS, fontScale = TmrTestDevices.LARGE_FONT_SCALE)
class InnerHeaderScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun wrappedSubtitle_atLargeText() = runBlocking<Unit> {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                TmrScreen(sheet = false, header = {
                    TmrInnerHeader(
                        title = "Export preview",
                        subtitle = "Associate Analyst · Northwind GCC and Meridian Capital Partners",
                        onBack = {},
                        backContentDescription = "Back",
                    )
                }) {
                    TmrCard(Modifier.fillMaxSize()) {}
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = "InnerHeaderWrappedSubtitleFont200",
            device = TmrTestDevices.boardLargeFont,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }
}
