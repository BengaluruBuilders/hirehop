package com.hirehop.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.hirehop.app.navigation.TOP_LEVEL_NAV_ITEMS
import com.hirehop.core.designsystem.component.HhBackground
import com.hirehop.core.designsystem.component.HhNavigationSuiteScaffold
import com.hirehop.core.designsystem.component.HhNavigationSuiteScope
import com.hirehop.core.navigation.Navigator
import com.hirehop.core.navigation.toEntries
import com.hirehop.feature.analysis.impl.navigation.analysisEntry
import com.hirehop.feature.applications.impl.navigation.applicationDetailEntry
import com.hirehop.feature.applications.impl.navigation.applicationsEntry
import com.hirehop.feature.onboarding.impl.navigation.onboardingEntry
import com.hirehop.feature.profile.impl.navigation.profileEntry
import com.hirehop.feature.tailor.impl.navigation.tailorEntry

@Composable
fun HhApp(
    appState: HhAppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val navigator = remember(appState.navigationState) { Navigator(appState.navigationState) }

    HhBackground(modifier = modifier) {
        HhNavigationSuiteScaffold(
            navigationSuiteItems = { topLevelNavItems(appState, navigator) },
            windowAdaptiveInfo = windowAdaptiveInfo,
        ) {
            HhNavDisplay(appState = appState, navigator = navigator)
        }
    }
}

@Composable
private fun HhNavDisplay(
    appState: HhAppState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val entryProvider = entryProvider {
        applicationsEntry(navigator)
        applicationDetailEntry(navigator)
        onboardingEntry(navigator)
        profileEntry(navigator)
        analysisEntry(navigator)
        tailorEntry(navigator)
    }

    Box(modifier = modifier) {
        NavDisplay(
            entries = appState.navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
        )
    }
}

private fun HhNavigationSuiteScope.topLevelNavItems(
    appState: HhAppState,
    navigator: Navigator,
) {
    TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
        item(
            selected = navKey == appState.currentTopLevelKey,
            onClick = { navigator.navigate(navKey) },
            icon = {
                Icon(imageVector = navItem.unselectedIcon, contentDescription = null)
            },
            selectedIcon = {
                Icon(imageVector = navItem.selectedIcon, contentDescription = null)
            },
            label = { Text(stringResource(navItem.labelRes)) },
        )
    }
}
