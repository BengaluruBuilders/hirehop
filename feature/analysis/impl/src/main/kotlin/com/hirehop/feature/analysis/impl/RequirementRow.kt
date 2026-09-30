package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.model.MatchStatus

@Composable
internal fun RequirementRow(
    item: RequirementItem,
    onIHaveThis: () -> Unit,
    onTogglePrepPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RequirementHeader(item)
            if (item.evidence.isNotEmpty()) EvidenceList(item.evidence)
            if (item.isGap) GapActions(item, onIHaveThis, onTogglePrepPlan)
        }
    }
}

@Composable
private fun RequirementHeader(item: RequirementItem) {
    val statusLabel = stringResource(item.status.labelRes())
    val priorityLabel = stringResource(item.priorityLabelRes())
    val color = item.status.color()
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = item.status.icon(),
            contentDescription = statusLabel,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = item.requirement.text, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = priorityLabel,
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun EvidenceList(evidence: List<String>) {
    Column(
        modifier = Modifier.padding(start = 36.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_analysis_evidence_header),
            style = MaterialTheme.typography.labelSmall,
        )
        evidence.forEach { text ->
            Text(text = "• $text", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GapActions(
    item: RequirementItem,
    onIHaveThis: () -> Unit,
    onTogglePrepPlan: () -> Unit,
) {
    val requirementText = item.requirement.text
    val iHaveThisDescription = stringResource(R.string.feature_analysis_i_have_this_description, requirementText)
    val prepDescription = stringResource(R.string.feature_analysis_add_to_prep_plan_description, requirementText)
    val prepState = stringResource(
        if (item.isInPrepPlan) {
            R.string.feature_analysis_prep_state_added
        } else {
            R.string.feature_analysis_prep_state_not_added
        },
    )
    Row(
        modifier = Modifier.padding(start = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HhOutlinedButton(
            onClick = onIHaveThis,
            modifier = Modifier.semantics { contentDescription = iHaveThisDescription },
            text = { Text(stringResource(R.string.feature_analysis_i_have_this)) },
        )
        TextButton(
            onClick = onTogglePrepPlan,
            modifier = Modifier.semantics {
                contentDescription = prepDescription
                stateDescription = prepState
                role = Role.Switch
            },
        ) {
            val labelRes = if (item.isInPrepPlan) {
                R.string.feature_analysis_in_prep_plan
            } else {
                R.string.feature_analysis_add_to_prep_plan
            }
            Text(stringResource(labelRes))
        }
    }
}

@StringRes
private fun MatchStatus.labelRes(): Int = when (this) {
    MatchStatus.MET -> R.string.feature_analysis_status_met
    MatchStatus.PARTIAL -> R.string.feature_analysis_status_partial
    MatchStatus.GAP -> R.string.feature_analysis_status_gap
}

@StringRes
private fun RequirementItem.priorityLabelRes(): Int = if (isMustHave) {
    R.string.feature_analysis_priority_must_have
} else {
    R.string.feature_analysis_priority_nice_to_have
}

private fun MatchStatus.icon(): ImageVector = when (this) {
    MatchStatus.MET -> HhIcons.Check
    MatchStatus.PARTIAL -> Icons.Rounded.Warning
    MatchStatus.GAP -> HhIcons.Close
}

@Composable
private fun MatchStatus.color(): Color = when (this) {
    MatchStatus.MET -> MaterialTheme.colorScheme.tertiary
    MatchStatus.PARTIAL -> MaterialTheme.colorScheme.secondary
    MatchStatus.GAP -> MaterialTheme.colorScheme.error
}
