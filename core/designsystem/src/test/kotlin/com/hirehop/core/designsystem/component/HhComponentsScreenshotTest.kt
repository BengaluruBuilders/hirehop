package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.icon.HhIcons
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
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class HhComponentsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    private fun capture(screenName: String, content: @Composable () -> Unit) = runBlocking<Unit> {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                Column(
                    modifier = Modifier.fillMaxSize().background(HhTheme.colors.background).padding(HhTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                ) { content() }
            }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = screenName,
            device = HhTestDevices.board,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    @Test
    fun solidCard() = capture("HhSolidCard") {
        HhSolidCard(
            accent = HhAccent.Coral,
            monogram = "LH",
            title = "Data Analyst",
            subtitle = "Lumen Health · Pune",
            openAction = HhOpenAction("Open Data Analyst") {},
            chips = {
                HhOnColorChip("Resume ready")
                HhOnColorChip("11 met", style = HhOnColorChipStyle.Outline)
            },
        )
        HhSolidCard(
            accent = HhAccent.Jade,
            monogram = "MC",
            title = "Product Analyst",
            subtitle = "Meridian Capital · Mumbai",
            openAction = HhOpenAction("Open Product Analyst") {},
        )
        HhSolidCard(
            accent = HhAccent.Marigold,
            monogram = "NB",
            title = "Operations Associate",
            subtitle = "Northbay Logistics · Hyderabad",
            chips = { HhOnColorChip("Draft", style = HhOnColorChipStyle.Ink) },
        )
    }

    @Test
    fun pillRows() = capture("HhPillRow") {
        HhPillRowStyle.entries.forEach { style ->
            HhPillRow(title = style.name, subtitle = "Short supporting line", onClick = {}, style = style, monogram = "HH")
        }
    }

    @Test
    fun statusRows() = capture("HhStatusRow") {
        HhStatusRow(HhStatusKind.Met, "Unit testing", "Met")
        HhStatusRow(HhStatusKind.Partial, "System design", "Partly met")
        HhStatusRow(HhStatusKind.Gap, "Kotlin coroutines", "To prepare")
    }

    @Test
    fun iconActionBar() = capture("HhIconActionBar") {
        HhIconActionBar(
            secondaryIcon = HhIcons.Add,
            secondaryContentDescription = "Add",
            onSecondaryClick = {},
            primaryLabel = "Tailor my resume",
            onPrimaryClick = {},
        )
        HhIconActionBar(
            secondaryIcon = HhIcons.Add,
            secondaryContentDescription = "Add",
            onSecondaryClick = {},
            primaryLabel = "Preview export",
            onPrimaryClick = {},
            secondaryBadge = "2",
            ink = true,
        )
    }

    @Test
    fun compactButtons() = capture("HhCompactButtons") {
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlineButton("I have this", onClick = {}, size = HhButtonSize.Compact)
            HhPrimaryButton("Add", onClick = {}, size = HhButtonSize.Compact)
            HhSecondaryButton("Skip", onClick = {}, size = HhButtonSize.Compact)
        }
        HhOutlineButton("I have this", onClick = {})
    }

    @Test
    fun dock() = capture("HhDock") {
        HhDock {
            HhDockItem(selected = true, onClick = {}, label = "Applications") { HhDockIcon(HhIcons.Applications) }
            HhDockItem(selected = false, onClick = {}, label = "Profile") { HhDockIcon(HhIcons.Profile) }
            HhDockItem(selected = false, onClick = {}, label = "Settings") { HhDockIcon(HhIcons.Settings) }
        }
    }
}
