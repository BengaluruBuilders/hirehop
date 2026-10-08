package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.icon.TmrIcons
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
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class TmrComponentsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    private fun capture(screenName: String, content: @Composable () -> Unit) = runBlocking<Unit> {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                Column(
                    modifier = Modifier.fillMaxSize().background(TmrTheme.colors.background).padding(TmrTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
                ) { content() }
            }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = screenName,
            device = TmrTestDevices.board,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    @Test
    fun pillRows() = capture("TmrPillRow") {
        TmrPillRowStyle.entries.forEach { style ->
            TmrPillRow(title = style.name, subtitle = "Short supporting line", onClick = {}, style = style, monogram = "TM")
        }
    }

    @Test
    fun iconActionBar() = capture("TmrIconActionBar") {
        TmrIconActionBar(
            secondaryIcon = TmrIcons.Add,
            secondaryContentDescription = "Add",
            onSecondaryClick = {},
            primaryLabel = "Tailor my resume",
            onPrimaryClick = {},
        )
        TmrIconActionBar(
            secondaryIcon = TmrIcons.Add,
            secondaryContentDescription = "Add",
            onSecondaryClick = {},
            primaryLabel = "Preview export",
            onPrimaryClick = {},
            secondaryBadge = "2",
            ink = true,
        )
    }

    @Test
    fun compactButtons() = capture("TmrCompactButtons") {
        Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            TmrOutlineButton("I have this", onClick = {}, size = TmrButtonSize.Compact)
            TmrPrimaryButton("Add", onClick = {}, size = TmrButtonSize.Compact)
            TmrSecondaryButton("Skip", onClick = {}, size = TmrButtonSize.Compact)
        }
        TmrOutlineButton("I have this", onClick = {})
    }

    @Test
    fun dock() = capture("TmrDock") {
        TmrDock {
            TmrDockItem(selected = true, onClick = {}, label = "Applications") { TmrDockIcon(TmrIcons.Applications) }
            TmrDockItem(selected = false, onClick = {}, label = "Profile") { TmrDockIcon(TmrIcons.Profile) }
            TmrDockItem(selected = false, onClick = {}, label = "Settings") { TmrDockIcon(TmrIcons.Settings) }
        }
    }

    @Test
    fun dockAt200Percent() = capture("TmrDockFont200") {
        CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
            TmrDock {
                TmrDockItem(selected = true, onClick = {}, label = "Applications") { TmrDockIcon(TmrIcons.Applications) }
                TmrDockItem(selected = false, onClick = {}, label = "Profile") { TmrDockIcon(TmrIcons.Profile) }
                TmrDockItem(selected = false, onClick = {}, label = "Settings") { TmrDockIcon(TmrIcons.Settings) }
            }
        }
    }
}
