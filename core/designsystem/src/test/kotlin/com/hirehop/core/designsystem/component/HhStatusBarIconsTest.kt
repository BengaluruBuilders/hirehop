package com.hirehop.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HhStatusBarIconsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun darkIconsAfter(darkTheme: Boolean, screen: @Composable () -> Unit): Boolean {
        rule.setContent { HhPreviewTheme(darkTheme = darkTheme, content = screen) }
        rule.waitForIdle()
        val window = rule.activity.window
        return WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars
    }

    @Test
    fun whiteTopScreenInLightThemeUsesDarkIcons() {
        assertEquals(true, darkIconsAfter(darkTheme = false) { HhScreen { } })
    }

    @Test
    fun jadeHeaderScreenUsesLightIcons() {
        assertEquals(
            false,
            darkIconsAfter(darkTheme = false) { HhScreen(header = { HhInnerHeader(title = "T") }) { } },
        )
    }

    @Test
    fun whiteTopScreenInDarkThemeUsesLightIcons() {
        assertEquals(false, darkIconsAfter(darkTheme = true) { HhScreen { } })
    }
}
