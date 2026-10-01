package com.hirehop.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import com.hirehop.app.R
import com.hirehop.app.navigation.TOP_LEVEL_NAV_ITEMS
import com.hirehop.app.ui.HhApp
import com.hirehop.app.ui.HhAppState
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.navigation.NavigationState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DebugScenarioActivity : ComponentActivity() {

    private var target by mutableStateOf(DebugScenarioTarget.Applications)
    private var scenario by mutableStateOf(DebugScenario.defaultValue)
    private var opened by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HhTheme {
                if (opened) {
                    DebugScenarioPreview(
                        target = target,
                        scenario = scenario,
                        onResetToDefault = ::resetToDefault,
                    )
                } else {
                    DebugScenarioMenu(
                        selectedTarget = target,
                        selectedScenario = scenario,
                        onTargetSelected = { target = it },
                        onScenarioSelected = { scenario = it },
                        onOpen = { opened = true },
                        onReset = ::resetToDefault,
                    )
                }
            }
        }
    }

    private fun resetToDefault() {
        target = DebugScenarioTarget.Applications
        scenario = DebugScenario.defaultValue
    }
}

@Composable
private fun DebugScenarioPreview(
    target: DebugScenarioTarget,
    scenario: DebugScenario,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startKey = target.navKey(scenario)
    val topLevelStack = rememberNavBackStack(startKey)
    val subStacks = TOP_LEVEL_NAV_ITEMS.keys.associateWith { key ->
        rememberNavBackStack(key.withScenario(scenario))
    }
    val navigationState = remember(startKey, subStacks) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.debug_scenario_preview_title, scenario.name),
                navigationIcon = HhIcons.ArrowBack,
                navigationIconContentDescription = stringResource(R.string.debug_scenario_back),
                onNavigationClick = onResetToDefault,
                actions = {
                    HhButton(
                        onClick = onResetToDefault,
                        text = { Text(text = stringResource(R.string.debug_scenario_reset)) },
                    )
                },
            )
        },
    ) { padding ->
        HhApp(
            appState = HhAppState(navigationState),
            modifier = Modifier.padding(padding),
        )
    }
}
