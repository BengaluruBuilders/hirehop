package com.tailormyresume.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

private object TestFirstTopLevelKey : NavKey
private object TestSecondTopLevelKey : NavKey
private object TestThirdTopLevelKey : NavKey
private object TestKeyFirst : NavKey
private object TestKeySecond : NavKey

class NavigatorTest {

    private lateinit var navigationState: NavigationState
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        val startKey = TestFirstTopLevelKey
        val topLevelStack = NavBackStack<NavKey>(startKey)
        val topLevelKeys = listOf(
            startKey,
            TestSecondTopLevelKey,
            TestThirdTopLevelKey,
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
    fun testStartKey() {
        assertThat(navigationState.startKey).isEqualTo(TestFirstTopLevelKey)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun testNavigate() {
        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
        assertThat(navigationState.subStacks[TestFirstTopLevelKey]?.last()).isEqualTo(TestKeyFirst)
    }

    @Test
    fun testNavigateTopLevel() {
        navigator.navigate(TestSecondTopLevelKey)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestSecondTopLevelKey)
    }

    @Test
    fun testNavigateSingleTop() {
        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
        ).inOrder()

        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
        ).inOrder()
    }

    @Test
    fun testNavigateTopLevelSingleTop() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestSecondTopLevelKey,
            TestKeyFirst,
        ).inOrder()

        navigator.navigate(TestSecondTopLevelKey)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestSecondTopLevelKey,
        ).inOrder()
    }

    @Test
    fun testSubStack() {
        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentKey).isEqualTo(TestKeyFirst)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)

        navigator.navigate(TestKeySecond)

        assertThat(navigationState.currentKey).isEqualTo(TestKeySecond)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun testMultiStack() {
        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentKey).isEqualTo(TestKeyFirst)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)

        navigator.navigate(TestSecondTopLevelKey)

        assertThat(navigationState.currentKey).isEqualTo(TestSecondTopLevelKey)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestSecondTopLevelKey)

        navigator.navigate(TestKeySecond)

        assertThat(navigationState.currentKey).isEqualTo(TestKeySecond)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestSecondTopLevelKey)

        navigator.navigate(TestFirstTopLevelKey)

        assertThat(navigationState.currentKey).isEqualTo(TestKeyFirst)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun testPopOneNonTopLevel() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
            TestKeySecond,
        ).inOrder()

        navigator.goBack()

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
        ).inOrder()

        assertThat(navigationState.currentKey).isEqualTo(TestKeyFirst)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun testPopOneTopLevel() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestSecondTopLevelKey)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestSecondTopLevelKey,
        ).inOrder()

        assertThat(navigationState.currentKey).isEqualTo(TestSecondTopLevelKey)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestSecondTopLevelKey)

        navigator.goBack()

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
        ).inOrder()

        assertThat(navigationState.currentKey).isEqualTo(TestKeyFirst)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun popMultipleNonTopLevel() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
            TestKeySecond,
        ).inOrder()

        navigator.goBack()
        navigator.goBack()

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
        ).inOrder()

        assertThat(navigationState.currentKey).isEqualTo(TestFirstTopLevelKey)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun popMultipleTopLevel() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestSecondTopLevelKey,
            TestKeyFirst,
        ).inOrder()

        navigator.navigate(TestThirdTopLevelKey)
        navigator.navigate(TestKeySecond)

        assertThat(navigationState.currentSubStack).containsExactly(
            TestThirdTopLevelKey,
            TestKeySecond,
        ).inOrder()

        repeat(4) {
            navigator.goBack()
        }

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
        ).inOrder()

        assertThat(navigationState.currentKey).isEqualTo(TestFirstTopLevelKey)
        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun goBackOnTheStartKeyReturnsFalseAndKeepsTheStack() {
        val handled = navigator.goBack()

        assertThat(handled).isFalse()
        assertThat(navigationState.currentKey).isEqualTo(TestFirstTopLevelKey)
        assertThat(navigationState.topLevelStack).containsExactly(TestFirstTopLevelKey)
    }

    @Test
    fun goBackReturnsTrueWhenItPopsAScreen() {
        navigator.navigate(TestKeyFirst)

        assertThat(navigator.goBack()).isTrue()
        assertThat(navigator.goBack()).isFalse()
    }

    @Test
    fun canGoBackIsFalseOnlyOnTheStartKey() {
        assertThat(navigationState.canGoBack).isFalse()

        navigator.navigate(TestKeyFirst)
        assertThat(navigationState.canGoBack).isTrue()

        navigator.navigate(TestSecondTopLevelKey)
        assertThat(navigationState.canGoBack).isTrue()

        navigator.goBack()
        navigator.goBack()
        assertThat(navigationState.canGoBack).isFalse()
    }

    @Test
    fun navigateAllPushesTheKeysInOrderOnTheCurrentTab() {
        navigator.navigateAll(listOf(TestKeyFirst, TestKeySecond))

        assertThat(navigationState.currentSubStack).containsExactly(
            TestFirstTopLevelKey,
            TestKeyFirst,
            TestKeySecond,
        ).inOrder()
    }

    @Test
    fun navigateAllWithNoKeysChangesNothing() {
        navigator.navigateAll(emptyList())

        assertThat(navigationState.currentSubStack).containsExactly(TestFirstTopLevelKey)
    }

    @Test
    fun aSingleStackRootGoesBackToItsStartAndStopsThere() {
        val start = TestFirstTopLevelKey
        val single = NavigationState(
            startKey = start,
            topLevelStack = NavBackStack(start),
            subStacks = mapOf(start to NavBackStack(start)),
        )
        val singleNavigator = Navigator(single)

        singleNavigator.navigate(TestKeyFirst)
        singleNavigator.navigate(TestKeySecond)

        assertThat(singleNavigator.goBack()).isTrue()
        assertThat(singleNavigator.goBack()).isTrue()
        assertThat(singleNavigator.goBack()).isFalse()
        assertThat(single.currentKey).isEqualTo(start)
    }

    @Test
    fun replaceSwapsTheTopKeyOfTheCurrentStack() {
        navigator.navigate(TestKeyFirst)

        navigator.replace(TestKeySecond)

        assertThat(navigationState.currentSubStack.toList())
            .containsExactly(TestFirstTopLevelKey, TestKeySecond).inOrder()
    }

    @Test
    fun replaceOnARootKeyActsLikeNavigate() {
        navigator.replace(TestKeyFirst)

        assertThat(navigationState.currentSubStack.toList())
            .containsExactly(TestFirstTopLevelKey, TestKeyFirst).inOrder()
    }

    @Test
    fun goBackAfterReplaceSkipsTheReplacedKey() {
        navigator.navigate(TestKeyFirst)
        navigator.replace(TestKeySecond)

        navigator.goBack()

        assertThat(navigationState.currentKey).isEqualTo(TestFirstTopLevelKey)
    }

    @Test
    fun replaceWithATopLevelKeySwitchesTheTab() {
        navigator.navigate(TestKeyFirst)

        navigator.replace(TestSecondTopLevelKey)

        assertThat(navigationState.currentTopLevelKey).isEqualTo(TestSecondTopLevelKey)
    }
}
