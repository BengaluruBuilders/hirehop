package com.hirehop.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Composable
    private fun WhiteBar() {
        Box(Modifier.fillMaxWidth().statusBarsPadding().height(48.dp))
    }

    @Test
    fun headerlessWhiteTopInLightThemeUsesDarkIcons() {
        assertTrue(darkIconsAfter(darkTheme = false) { HhScreen { } })
    }

    @Test
    fun whiteInnerHeaderInLightThemeUsesDarkIcons() {
        assertTrue(darkIconsAfter(darkTheme = false) { HhScreen(header = { HhInnerHeader(title = "T") }) { } })
    }

    @Test
    fun lightThemeHeaderTopUsesDarkIcons() {
        assertTrue(darkIconsAfter(darkTheme = false) { HhScreen(lightTop = false) { } })
    }

    @Test
    fun whiteHeaderWithLightTopTrueUsesDarkIcons() {
        assertTrue(darkIconsAfter(darkTheme = false) { HhScreen(lightTop = true, header = { WhiteBar() }) { } })
    }

    @Test
    fun darkThemeUsesLightIcons() {
        assertFalse(darkIconsAfter(darkTheme = true) { HhScreen(lightTop = true, header = { WhiteBar() }) { } })
    }
}
