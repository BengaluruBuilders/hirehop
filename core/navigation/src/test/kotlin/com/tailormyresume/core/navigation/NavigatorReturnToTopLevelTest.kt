package com.tailormyresume.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test

private data class ReturnTab(val scenario: Int = 0) : NavKey

private object ReturnOtherTab : NavKey

private object ReturnDetail : NavKey

class NavigatorReturnToTopLevelTest {

    @Test
    fun returnsToTheTabWhoseKeyCarriesAnotherScenarioAndClearsItsSubStack() {
        val forced = ReturnTab(scenario = 3)
        val keys = listOf(forced, ReturnOtherTab)
        val state = NavigationState(
            startKey = forced,
            topLevelStack = NavBackStack<NavKey>(forced),
            subStacks = keys.associateWith { NavBackStack(it) },
        )
        val navigator = Navigator(state)
        navigator.navigate(ReturnDetail)

        navigator.returnToTopLevel(ReturnTab())

        assertThat(state.currentTopLevelKey).isEqualTo(forced)
        assertThat(state.currentKey).isEqualTo(forced)
        assertThat(state.subStacks.getValue(forced)).containsExactly(forced)
    }
}
