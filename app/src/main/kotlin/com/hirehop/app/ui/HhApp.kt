package com.hirehop.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.hirehop.app.navigation.START_NAV_KEY
import com.hirehop.app.navigation.TOP_LEVEL_NAV_ITEMS
import com.hirehop.app.navigation.isTopLevelDestination
import com.hirehop.core.designsystem.component.HhBackground
import com.hirehop.core.designsystem.component.HhDock
import com.hirehop.core.designsystem.component.HhDockDefaults
import com.hirehop.core.designsystem.component.HhDockIcon
import com.hirehop.core.designsystem.component.HhDockItem
import com.hirehop.core.designsystem.component.LocalHhBottomInset
import com.hirehop.core.designsystem.component.rememberHhNavTransitions
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.navigation.NavigationState
import com.hirehop.core.navigation.Navigator
import com.hirehop.core.navigation.PendingNavigation
import com.hirehop.core.navigation.rememberNavigationState
import com.hirehop.core.navigation.toEntries
import com.hirehop.feature.analysis.impl.navigation.analysisEntry
import com.hirehop.feature.applications.impl.navigation.applicationDetailEntry
import com.hirehop.feature.applications.impl.navigation.applicationsEntry
import com.hirehop.feature.onboarding.api.navigation.DefaultWelcomeNavKey
import com.hirehop.feature.onboarding.impl.navigation.onboardingEntry
import com.hirehop.feature.profile.impl.navigation.profileEntry
import com.hirehop.feature.settings.impl.navigation.settingsEntry
import com.hirehop.feature.tailor.impl.navigation.tailorEntry

@Composable
fun HhApp(
    rootState: AppRootState,
    modifier: Modifier = Modifier,
) {
    val stores = rememberRootViewModelStores()
    LaunchedEffect(rootState) {
        when (rootState) {
            AppRootState.Loading -> Unit
            AppRootState.FirstRun -> stores.keepOnly(NavigationRoot.FirstRun)
            AppRootState.Main -> stores.keepOnly(NavigationRoot.Main)
        }
    }
    HhBackground(modifier = modifier) {
        when (rootState) {
            AppRootState.Loading -> Unit
            AppRootState.FirstRun -> HhFirstRunRoot()
            AppRootState.Main -> HhMainRoot()
        }
    }
}

@Composable
internal fun HhFirstRunRoot(
    modifier: Modifier = Modifier,
    startKey: NavKey = DefaultWelcomeNavKey,
    initialKeys: () -> List<NavKey> = PendingNavigation::consume,
) {
    WithRootViewModelStore(NavigationRoot.FirstRun) {
        val navigationState = rememberNavigationState(startKey, setOf(startKey))
        val navigator = remember(navigationState) {
            Navigator(navigationState).also { it.navigateAll(initialKeys()) }
        }
        HhNavDisplay(
            navigationState = navigationState,
            navigator = navigator,
            dockInset = 0.dp,
            modifier = modifier,
        )
    }
}

@Composable
internal fun HhMainRoot(
    modifier: Modifier = Modifier,
    initialKeys: () -> List<NavKey> = PendingNavigation::consume,
) {
    WithRootViewModelStore(NavigationRoot.Main) {
        HhMainRootContent(modifier = modifier, initialKeys = initialKeys)
    }
}

@Composable
private fun HhMainRootContent(
    modifier: Modifier,
    initialKeys: () -> List<NavKey>,
) {
    val navigationState = rememberNavigationState(START_NAV_KEY, TOP_LEVEL_NAV_ITEMS.keys)
    val navigator = remember(navigationState) {
        Navigator(navigationState).also { it.navigateAll(initialKeys()) }
    }
    Box(modifier = modifier.fillMaxSize()) {
        HhNavDisplay(
            navigationState = navigationState,
            navigator = navigator,
            dockInset = HhDockDefaults.inset,
        )
        AnimatedVisibility(
            visible = navigationState.currentKey.isTopLevelDestination(),
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(HhTheme.motion.proofSpecs.offset) { it } +
                fadeIn(HhTheme.motion.proofSpecs.fade),
            exit = slideOutVertically(HhTheme.motion.proofSpecs.offset) { it } +
                fadeOut(HhTheme.motion.proofSpecs.fade),
        ) {
            HhMainDock(navigationState = navigationState, navigator = navigator)
        }
    }
}

@Composable
private fun HhMainDock(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    HhDock(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                bottom = HhDockDefaults.floatGap,
            ),
    ) {
        TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
            val label = stringResource(navItem.labelRes)
            val selected = navKey == navigationState.currentTopLevelKey
            HhDockItem(
                selected = selected,
                onClick = { navigator.navigate(navKey) },
                contentDescription = label,
                icon = { HhDockIcon(if (selected) navItem.selectedIcon else navItem.unselectedIcon) },
                label = { Text(text = label) },
            )
        }
    }
}

@Composable
private fun HhNavDisplay(
    navigationState: NavigationState,
    navigator: Navigator,
    dockInset: Dp,
    modifier: Modifier = Modifier,
) {
    val entryProvider = remember(navigator, dockInset) {
        withDockInset(sharedEntryProvider(navigator), dockInset)
    }
    val transitions = rememberHhNavTransitions()
    Box(modifier = modifier) {
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            transitionSpec = { transitions.forward(this, hierarchical = !targetState.isTopLevel()) },
            popTransitionSpec = { transitions.back(hierarchical = !initialState.isTopLevel()) },
            predictivePopTransitionSpec = { transitions.back(hierarchical = !initialState.isTopLevel()) },
        )
    }
}

private fun Scene<NavKey>.isTopLevel(): Boolean = metadata[TOP_LEVEL_METADATA] == true

private const val TOP_LEVEL_METADATA = "hhTopLevel"

private fun sharedEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        applicationsEntry(navigator)
        applicationDetailEntry(navigator)
        onboardingEntry(navigator)
        profileEntry(navigator)
        analysisEntry(navigator)
        tailorEntry(navigator)
        settingsEntry(navigator)
    }

private fun withDockInset(
    provider: (NavKey) -> NavEntry<NavKey>,
    dockInset: Dp,
): (NavKey) -> NavEntry<NavKey> = { key ->
    val entry = provider(key)
    val topLevel = key.isTopLevelDestination()
    val metadata = entry.metadata + (TOP_LEVEL_METADATA to topLevel)
    NavEntry(key = key, contentKey = entry.contentKey, metadata = metadata) {
        CompositionLocalProvider(LocalHhBottomInset provides if (topLevel) dockInset else 0.dp) { entry.Content() }
    }
}
