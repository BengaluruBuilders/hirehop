package com.tailormyresume.app.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.app.ui.TmrShell
import com.tailormyresume.core.designsystem.component.chrome.TmrTopBarLeading
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.navigation.NavigationState
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.core.navigation.rememberNavigationState

private val STEP_DESCRIPTIONS = listOf("Step 1 of 3, Profile", "Step 2 of 3, Job", "Step 3 of 3, Tailor")

internal class ShellHarness(private val rule: ComposeContentTestRule) {

    var hasHome: Boolean = false

    lateinit var navigator: Navigator
        private set

    lateinit var state: NavigationState
        private set

    val stack: List<NavKey> get() = state.stack.toList()

    fun show(
        start: NavKey,
        entries: ((Navigator) -> (NavKey) -> NavEntry<NavKey>)? = null,
        fontScale: Float = 1f,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                TmrTheme {
                    val navigationState = rememberNavigationState(start)
                    val shellNavigator = remember(navigationState) { shellNavigator(navigationState) { hasHome } }
                    state = navigationState
                    navigator = shellNavigator
                    if (entries == null) {
                        TmrShell(navigationState, shellNavigator)
                    } else {
                        TmrShell(navigationState, shellNavigator, entryProvider = entries(shellNavigator))
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    fun go(vararg keys: NavKey) {
        rule.runOnIdle { keys.forEach(navigator::navigate) }
        rule.waitForIdle()
    }

    fun root(key: NavKey) {
        rule.runOnIdle { navigator.root(key) }
        rule.waitForIdle()
    }

    fun clickBack() = click("Back")

    fun clickClose() = click("Close")

    fun click(description: String) {
        rule.onNode(hasContentDescription(description)).performClick()
        rule.waitForIdle()
    }

    fun assertChrome(row: ChromeRow, leading: TmrTopBarLeading = row.leading) {
        val label = row.key::class.simpleName
        val stepNodes = rule.onAllNodes(hasContentDescription("Step ", substring = true))
        if (row.step == null) {
            stepNodes.assertCountEquals(0)
        } else {
            rule.onAllNodes(hasContentDescription(STEP_DESCRIPTIONS.getValue(row.step - 1))).assertCountEquals(1)
        }
        rule.onAllNodes(hasContentDescription("Back")).assertCountEquals(if (leading == TmrTopBarLeading.Back) 1 else 0)
        rule.onAllNodes(hasContentDescription("Close")).assertCountEquals(if (leading == TmrTopBarLeading.Close) 1 else 0)
        listOf("Resume", "Application", "Experience", "Settings").filter { it != row.title }.forEach { other ->
            rule.onAllNodesWithText(other, ignoreCase = true).assertCountEquals(0)
        }
        row.title?.let { rule.onNodeWithText(it, ignoreCase = true).assertExists("$label title") }
        listOf("Save", "Done").filter { it != row.action }.forEach { other ->
            rule.onAllNodesWithText(other).assertCountEquals(0)
        }
        row.action?.let { rule.onNodeWithText(it).assertExists("$label action") }
    }
}
