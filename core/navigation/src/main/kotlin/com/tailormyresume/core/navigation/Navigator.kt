package com.tailormyresume.core.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(val state: NavigationState) {

    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    fun openInOwnTab(key: NavKey) {
        state.topLevelKeys.firstOrNull { topLevel -> topLevel::class == key::class }?.let(::navigate)
        navigate(key)
    }

    fun returnToTopLevel(key: NavKey) {
        navigate(state.topLevelKeys.firstOrNull { topLevel -> topLevel::class == key::class } ?: key)
    }

    fun replace(key: NavKey) {
        val stack = state.currentSubStack
        if (key in state.topLevelKeys || stack.size <= 1) {
            navigate(key)
            return
        }
        stack.removeLastOrNull()
        goToKey(key)
    }

    fun navigateAll(keys: List<NavKey>) {
        keys.forEach(::navigate)
    }

    fun goBack(): Boolean {
        if (!state.canGoBack) return false
        when (state.currentKey) {
            state.currentTopLevelKey -> state.topLevelStack.removeLastOrNull()
            else -> state.currentSubStack.removeLastOrNull()
        }
        return true
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
