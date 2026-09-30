package com.hirehop.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import com.hirehop.app.navigation.START_NAV_KEY
import com.hirehop.app.navigation.TOP_LEVEL_NAV_ITEMS
import com.hirehop.core.navigation.NavigationState
import com.hirehop.core.navigation.rememberNavigationState

@Composable
fun rememberHhAppState(): HhAppState {
    val navigationState = rememberNavigationState(START_NAV_KEY, TOP_LEVEL_NAV_ITEMS.keys)
    return remember(navigationState) { HhAppState(navigationState) }
}

@Stable
class HhAppState(val navigationState: NavigationState) {
    val currentTopLevelKey: NavKey
        get() = navigationState.currentTopLevelKey
}
