package com.hirehop.feature.settings.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS, fontScale = HhTestDevices.LARGE_FONT_SCALE)
class InnerHeaderScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun wrappedSubtitle_atLargeText() = runBlocking<Unit> {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                HhScreen(sheet = false, header = {
                    HhInnerHeader(
                        title = "Export preview",
                        subtitle = "Associate Analyst · Northwind GCC and Meridian Capital Partners",
                        onBack = {},
                        backContentDescription = "Back",
                    )
                }) {
                    HhCard(Modifier.fillMaxSize()) {}
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = "InnerHeaderWrappedSubtitleFont200",
            device = HhTestDevices.boardLargeFont,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }
}
