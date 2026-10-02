package com.hirehop.app.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.app.R
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhFilterChip
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSwitch
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.DebugScenario

data class DebugMenuActions(
    val onLoadSampleData: () -> Unit,
    val onResetAppData: () -> Unit,
    val onOpenApp: () -> Unit,
    val onOnlineChange: (Boolean) -> Unit,
    val onTargetSelected: (DebugScenarioTarget) -> Unit,
    val onScenarioSelected: (DebugScenario) -> Unit,
    val onOpenScreen: () -> Unit,
)

@Composable
fun DebugScenarioMenu(
    uiState: DebugMenuUiState,
    selectedTarget: DebugScenarioTarget,
    selectedScenario: DebugScenario,
    actions: DebugMenuActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.debug_scenario_title),
                subtitle = stringResource(R.string.debug_scenario_intro),
                extended = false,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            DebugDataCard(uiState = uiState, actions = actions)
            DebugConnectivityCard(online = uiState.online, onOnlineChange = actions.onOnlineChange)
            DebugScenarioCard(
                selectedTarget = selectedTarget,
                selectedScenario = selectedScenario,
                actions = actions,
            )
        }
    }
}

@Composable
private fun DebugDataCard(
    uiState: DebugMenuUiState,
    actions: DebugMenuActions,
) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        DebugHeading(R.string.debug_data_heading)
        val status = when {
            uiState.busy -> stringResource(R.string.debug_data_busy)
            uiState.message == DebugDataMessage.SampleLoaded -> stringResource(R.string.debug_data_loaded)
            uiState.message == DebugDataMessage.DataReset -> stringResource(R.string.debug_data_cleared)
            uiState.message == DebugDataMessage.Failed -> stringResource(R.string.debug_data_failed)
            else -> null
        }
        if (status != null) {
            Text(text = status, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurfaceVariant)
        }
        HhPrimaryButton(
            label = stringResource(R.string.debug_data_load),
            onClick = actions.onLoadSampleData,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.busy,
        )
        HhOutlineButton(
            label = stringResource(R.string.debug_data_reset),
            onClick = actions.onResetAppData,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.busy,
        )
        HhOutlineButton(
            label = stringResource(R.string.debug_data_open_app),
            onClick = actions.onOpenApp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DebugConnectivityCard(
    online: Boolean,
    onOnlineChange: (Boolean) -> Unit,
) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        DebugHeading(R.string.debug_connectivity_heading)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.debug_connectivity_online),
                style = HhTheme.typography.bodyL,
                color = HhTheme.colors.onSurface,
            )
            HhSwitch(checked = online, onCheckedChange = onOnlineChange)
        }
    }
}

@Composable
private fun DebugScenarioCard(
    selectedTarget: DebugScenarioTarget,
    selectedScenario: DebugScenario,
    actions: DebugMenuActions,
) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        DebugHeading(R.string.debug_scenario_destination_heading)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            DebugScenarioTarget.entries.forEach { target ->
                HhFilterChip(
                    label = stringResource(target.titleRes),
                    selected = target == selectedTarget,
                    onClick = { actions.onTargetSelected(target) },
                )
            }
        }
        DebugHeading(R.string.debug_scenario_state_heading)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            DebugScenario.entries.forEach { scenario ->
                HhFilterChip(
                    label = scenario.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                    selected = scenario == selectedScenario,
                    onClick = { actions.onScenarioSelected(scenario) },
                )
            }
        }
        HhPrimaryButton(
            label = stringResource(R.string.debug_scenario_open),
            onClick = actions.onOpenScreen,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DebugHeading(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = HhTheme.typography.titleM,
        color = HhTheme.colors.onSurface,
    )
}
