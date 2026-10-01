package com.hirehop.app.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.app.R
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhListRow
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.DebugScenario

@Composable
fun DebugScenarioMenu(
    selectedTarget: DebugScenarioTarget,
    selectedScenario: DebugScenario,
    onTargetSelected: (DebugScenarioTarget) -> Unit,
    onScenarioSelected: (DebugScenario) -> Unit,
    onOpen: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = { HhTopAppBar(title = stringResource(R.string.debug_scenario_title)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(HhTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.debug_scenario_intro),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            DebugScenarioTargetPicker(
                selected = selectedTarget,
                onSelected = onTargetSelected,
            )
            DebugScenarioStatePicker(
                selected = selectedScenario,
                onSelected = onScenarioSelected,
            )
            HhButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                text = { Text(text = stringResource(R.string.debug_scenario_open)) },
            )
            HhButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTarget != DebugScenarioTarget.Applications ||
                    selectedScenario != DebugScenario.defaultValue,
                text = { Text(text = stringResource(R.string.debug_scenario_reset)) },
            )
        }
    }
}

@Composable
private fun DebugScenarioTargetPicker(
    selected: DebugScenarioTarget,
    onSelected: (DebugScenarioTarget) -> Unit,
) {
    HhSectionCard {
        Text(
            text = stringResource(R.string.debug_scenario_destination_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        DebugScenarioTarget.entries.forEach { target ->
            HhListRow(
                showDivider = true,
                onClick = { onSelected(target) },
            ) {
                Text(
                    text = stringResource(target.titleRes),
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurface,
                )
                if (target == selected) {
                    Text(
                        text = stringResource(R.string.debug_scenario_selected),
                        style = HhTheme.typography.bodySmall,
                        color = HhTheme.colors.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugScenarioStatePicker(
    selected: DebugScenario,
    onSelected: (DebugScenario) -> Unit,
) {
    HhSectionCard {
        Text(
            text = stringResource(R.string.debug_scenario_state_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        DebugScenario.entries.forEach { scenario ->
            HhListRow(
                onClick = { onSelected(scenario) },
            ) {
                Text(
                    text = scenario.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurface,
                )
                if (scenario == selected) {
                    Text(
                        text = stringResource(R.string.debug_scenario_selected),
                        style = HhTheme.typography.bodySmall,
                        color = HhTheme.colors.primary,
                    )
                }
            }
        }
    }
}
