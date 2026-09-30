package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton

@Composable
internal fun ResultContent(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    modifier: Modifier = Modifier,
) {
    var evidenceRequirementId by rememberSaveable { mutableStateOf<String?>(null) }
    val evidenceTarget = state.sections
        .flatMap { it.items }
        .firstOrNull { it.id == evidenceRequirementId }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { CoverageCard(state) }
            item { JobDetailsFields(state, actions) }
            resultSections(state.sections, actions.onTogglePrepPlan) { evidenceRequirementId = it }
        }
        SaveBar(state, actions)
    }

    if (evidenceTarget != null) {
        EvidenceDialog(
            requirementText = evidenceTarget.requirement.text,
            onDismiss = { evidenceRequirementId = null },
            onConfirm = { statement ->
                actions.onSubmitEvidence(evidenceTarget.id, statement)
                evidenceRequirementId = null
            },
        )
    }
}

@Composable
private fun CoverageCard(state: AnalysisUiState.Result) {
    val coverage = state.keywordCoverage
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (coverage.total == 0) {
                Text(
                    text = stringResource(R.string.feature_analysis_coverage_empty),
                    style = MaterialTheme.typography.titleMedium,
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.feature_analysis_coverage_headline,
                        coverage.covered,
                        coverage.total,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Text(
                text = stringResource(R.string.feature_analysis_coverage_explanation),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun JobDetailsFields(state: AnalysisUiState.Result, actions: AnalysisActions) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.title,
            onValueChange = actions.onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.feature_analysis_field_title)) },
            singleLine = true,
            isError = !state.canSave,
        )
        OutlinedTextField(
            value = state.company,
            onValueChange = actions.onCompanyChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.feature_analysis_field_company)) },
            singleLine = true,
        )
        TextButton(onClick = actions.onEditJobText) {
            Text(stringResource(R.string.feature_analysis_edit_job_text))
        }
    }
}

private fun LazyListScope.resultSections(
    sections: List<RequirementSection>,
    onTogglePrepPlan: (String) -> Unit,
    onIHaveThis: (String) -> Unit,
) {
    sections.forEach { section ->
        item(key = "header-${section.group}") { GroupHeader(section) }
        items(section.items, key = { "${section.group}-${it.id}" }) { item ->
            RequirementRow(
                item = item,
                onIHaveThis = { onIHaveThis(item.id) },
                onTogglePrepPlan = { onTogglePrepPlan(item.id) },
            )
        }
    }
}

@Composable
private fun GroupHeader(section: RequirementSection) {
    Text(
        text = stringResource(
            R.string.feature_analysis_group_header,
            stringResource(section.group.titleRes()),
            section.items.size,
        ),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun SaveBar(state: AnalysisUiState.Result, actions: AnalysisActions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (state.prepPlanCount > 0) {
            Text(
                text = stringResource(R.string.feature_analysis_prep_plan_count, state.prepPlanCount),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        HhButton(
            onClick = actions.onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
            text = { Text(stringResource(R.string.feature_analysis_save_and_tailor)) },
        )
    }
}

@StringRes
private fun RequirementGroup.titleRes(): Int = when (this) {
    RequirementGroup.MustHaveGaps -> R.string.feature_analysis_group_must_have_gaps
    RequirementGroup.Partial -> R.string.feature_analysis_group_partial
    RequirementGroup.Met -> R.string.feature_analysis_group_met
    RequirementGroup.NiceToHaveGaps -> R.string.feature_analysis_group_nice_to_have_gaps
}
