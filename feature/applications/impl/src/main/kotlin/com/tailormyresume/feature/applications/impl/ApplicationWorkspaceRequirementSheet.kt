package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.tailormyresume.core.designsystem.component.TmrBottomSheet
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.ui.MatchStatusKindMapper

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun WorkspaceRequirementSheet(
    state: WorkspaceRequirementSheetState,
    onAddToPrepPlan: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (state.status) {
        MatchStatus.MET -> stringResource(R.string.feature_applications_impl_match_met)
        MatchStatus.PARTIAL -> stringResource(R.string.feature_applications_impl_match_partial)
        MatchStatus.GAP -> stringResource(R.string.feature_applications_impl_match_gap)
    }
    TmrBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            TmrStatusChip(
                kind = MatchStatusKindMapper().kindOf(state.status),
                label = statusLabel,
            )
            Text(
                text = state.name,
                style = TmrTheme.typography.headlineM,
                color = TmrTheme.colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(
                    R.string.feature_applications_impl_requirement_sheet_asked_for,
                    state.requirementText,
                ),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
            if (state.status != MatchStatus.GAP && state.evidenceIds.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.feature_applications_impl_requirement_sheet_facts_heading),
                    style = TmrTheme.typography.labelM,
                    color = TmrTheme.colors.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
                    state.evidenceIds.forEach { id -> TmrFactId(id = id) }
                }
            }
            if (state.status != MatchStatus.MET) {
                if (state.isInPrepPlan) {
                    TmrSecondaryButton(
                        label = stringResource(R.string.feature_applications_impl_requirement_sheet_in_plan),
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    TmrOutlineButton(
                        label = stringResource(R.string.feature_applications_impl_requirement_sheet_add),
                        onClick = onAddToPrepPlan,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
