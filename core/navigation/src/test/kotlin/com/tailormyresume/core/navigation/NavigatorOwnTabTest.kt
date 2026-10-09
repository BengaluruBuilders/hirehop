package com.tailormyresume.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

private data class TestTab(val scenario: Int = 0) : NavKey

private object TestOtherTab : NavKey

private object TestPlainKey : NavKey

class NavigatorOwnTabTest {

    private lateinit var navigationState: NavigationState
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        val startKey = TestTab()
        val topLevelStack = NavBackStack<NavKey>(startKey)
        val topLevelKeys = listOf(
            startKey,
            TestOtherTab,
        )
        val subStacks = topLevelKeys.associateWith { key -> NavBackStack(key) }

        navigationState = NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
        navigator = Navigator(navigationState)
    }

    @Test
    fun nonDefaultTopLevelKeySelectsItsOwnTab() {
        navigator.navigate(TestOtherTab)

        navigator.openInOwnTab(TestTab(5))

        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestTab())
        assertThat(navigationState.currentKey).isEqualTo(TestTab(5))
        assertThat(navigationState.subStacks[TestTab()]).containsExactly(
            TestTab(),
            TestTab(5),
        ).inOrder()
    }

    @Test
    fun defaultTopLevelKeyJustSelectsTheTab() {
        navigator.navigate(TestOtherTab)

        navigator.openInOwnTab(TestTab())

        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestTab())
        assertThat(navigationState.currentKey).isEqualTo(TestTab())
    }

    @Test
    fun plainKeyStaysOnCurrentTab() {
        navigator.openInOwnTab(TestPlainKey)

        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestTab())
        assertThat(navigationState.currentKey).isEqualTo(TestPlainKey)
        assertThat(navigationState.subStacks[TestTab()]).containsExactly(
            TestTab(),
            TestPlainKey,
        ).inOrder()
    }
}
