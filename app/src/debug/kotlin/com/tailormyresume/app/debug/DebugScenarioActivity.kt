package com.tailormyresume.app.debug

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.app.MainActivity
import com.tailormyresume.app.ui.AppRootState
import com.tailormyresume.app.ui.AppViewModel
import com.tailormyresume.app.ui.RootViewModelStores
import com.tailormyresume.app.ui.TmrFirstRunRoot
import com.tailormyresume.app.ui.TmrMainRoot
import com.tailormyresume.core.designsystem.component.TmrBackground
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.onboarding.api.navigation.DefaultWelcomeNavKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class DebugScenarioActivity : ComponentActivity() {

    private val menuViewModel: DebugMenuViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()
    private val rootStores: RootViewModelStores by viewModels { RootViewModelStores.Factory }

    private var target by mutableStateOf(DebugScenarioTarget.Applications)
    private var scenario by mutableStateOf(DebugScenario.defaultValue)
    private var opened by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContent {
            TmrTheme {
                BackHandler(enabled = opened) { closePreview() }
                TmrBackground {
                    if (opened) {
                        DebugScenarioPreview(target = target, scenario = scenario, rootState = appViewModel.rootState)
                    } else {
                        val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()
                        DebugScenarioMenu(
                            uiState = uiState,
                            selectedTarget = target,
                            selectedScenario = scenario,
                            actions = DebugMenuActions(
                                onLoadSampleData = menuViewModel::loadSampleData,
                                onResetAppData = menuViewModel::resetAppData,
                                onOpenApp = ::openApp,
                                onOnlineChange = menuViewModel::setOnline,
                                onTargetSelected = { target = it },
                                onScenarioSelected = { scenario = it },
                                onOpenScreen = ::openPreview,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun openPreview() {
        rootStores.releaseAll()
        menuViewModel.openPreview(target) { opened = true }
    }

    private fun closePreview() {
        opened = false
        rootStores.releaseAll()
        menuViewModel.closePreview(target)
    }

    private fun openApp() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}

@Composable
private fun DebugScenarioPreview(
    target: DebugScenarioTarget,
    scenario: DebugScenario,
    rootState: StateFlow<AppRootState>,
    modifier: Modifier = Modifier,
) {
    val key = target.navKey(scenario)
    val currentRoot by rootState.collectAsStateWithLifecycle()
    var seenMain by remember { mutableStateOf(false) }
    LaunchedEffect(currentRoot) {
        if (currentRoot == AppRootState.Main) seenMain = true
    }
    if (target.opensFirstRunRoot) {
        TmrFirstRunRoot(modifier = modifier, startKey = key)
    } else if (previewShowsWelcome(seenMain, currentRoot)) {
        TmrFirstRunRoot(modifier = modifier, startKey = DefaultWelcomeNavKey)
    } else {
        TmrMainRoot(modifier = modifier, initialKeys = { listOf(key) })
    }
}

internal fun previewShowsWelcome(seenMain: Boolean, rootState: AppRootState): Boolean =
    seenMain && rootState == AppRootState.FirstRun
