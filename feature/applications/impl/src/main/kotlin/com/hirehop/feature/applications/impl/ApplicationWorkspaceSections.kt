package com.hirehop.feature.applications.impl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.ui.ApplicationStatusKindMapper

private val TICK_BOX_SIZE: Dp = 24.dp
private val TICK_BOX_STROKE: Dp = 1.5.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WorkspaceGapSection(
    gapCounts: WorkspaceGapCounts?,
    coverage: KeywordCoverage?,
    modifier: Modifier = Modifier,
) {
    HhSectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_gap_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        if (gapCounts == null) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_gap_unavailable),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            return@HhSectionCard
        }
        val heading = stringResource(R.string.feature_applications_impl_workspace_gap_heading)
        val metLabel = stringResource(R.string.feature_applications_impl_workspace_gap_met)
        val partialLabel = stringResource(R.string.feature_applications_impl_workspace_gap_partial)
        val gapLabel = stringResource(R.string.feature_applications_impl_workspace_gap_gap)
        val description = pluralStringResource(
            id = R.plurals.feature_applications_impl_workspace_gap_description,
            gapCounts.gap,
            heading,
            gapCounts.met,
            gapCounts.partial,
            gapCounts.gap,
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d16),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            GapCount(kind = HhStatusKind.Met, count = gapCounts.met, label = metLabel)
            GapCount(kind = HhStatusKind.Partial, count = gapCounts.partial, label = partialLabel)
            GapCount(kind = HhStatusKind.Gap, count = gapCounts.gap, label = gapLabel)
        }
        if (coverage != null) {
            Text(
                text = coveragePhrase(coverage),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GapCount(
    kind: HhStatusKind,
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhStatusDisc(kind = kind)
        Text(
            text = count.toString(),
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = label,
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun WorkspaceJobDescriptionSection(
    text: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhSectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_jd_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(
                id = if (isExpanded) {
                    R.string.feature_applications_impl_workspace_jd_hide
                } else {
                    R.string.feature_applications_impl_workspace_jd_show
                },
            ),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48)
                .clickable(onClick = onToggle),
        )
        if (!isExpanded) return@HhSectionCard
        HhDivider()
        Text(
            text = text,
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun WorkspacePrepTaskRow(
    task: WorkspacePrepTask,
    onToggle: () -> Unit,
    onOverflowToggle: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val doneLabel = stringResource(R.string.feature_applications_impl_workspace_task_done)
    val notDoneLabel = stringResource(R.string.feature_applications_impl_workspace_task_not_done)
    val sourceLabel = stringResource(
        id = R.string.feature_applications_impl_workspace_task_source,
        task.priority.label(),
    )
    val description = stringResource(
        id = R.string.feature_applications_impl_workspace_task_description,
        task.requirementText,
        sourceLabel,
        if (task.isDone) doneLabel else notDoneLabel,
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48)
                .clickable(onClick = onToggle)
                .semantics(mergeDescendants = true) { contentDescription = description },
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            PrepTaskTick(
                isDone = task.isDone,
                modifier = Modifier.size(TICK_BOX_SIZE),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = task.requirementText,
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sourceLabel,
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            HhIconButton(
                icon = HhIcons.More,
                contentDescription = stringResource(R.string.feature_applications_impl_workspace_more),
                onClick = onOverflowToggle,
            )
        }
        if (task.isOverflowOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48)
                    .clickable(onClick = onReport)
                    .padding(horizontal = HhTheme.spacing.d8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.feature_applications_impl_workspace_task_report),
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
        HhDivider()
    }
}

@Composable
private fun PrepTaskTick(
    isDone: Boolean,
    modifier: Modifier = Modifier,
) {
    val outline = HhTheme.colors.onSurfaceVariant
    val filled = HhTheme.colors.primary
    val boxColor = if (isDone) filled else Color.Transparent
    val tickColor = if (isDone) HhTheme.colors.onPrimary else outline
    Canvas(modifier = modifier) {
        val stroke = TICK_BOX_STROKE.toPx()
        val inset = stroke / 2f
        val side = size.minDimension - stroke
        drawRect(
            color = boxColor,
            topLeft = Offset(inset, inset),
            size = Size(side, side),
        )
        drawRect(
            color = if (isDone) filled else outline,
            topLeft = Offset(inset, inset),
            size = Size(side, side),
            style = Stroke(width = stroke),
        )
        if (isDone) {
            val path = Path().apply {
                moveTo(size.width * 0.28f, size.height * 0.52f)
                lineTo(size.width * 0.44f, size.height * 0.68f)
                lineTo(size.width * 0.74f, size.height * 0.34f)
            }
            drawPath(
                path = path,
                color = tickColor,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
internal fun WorkspaceStatusChip(
    status: ApplicationStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = status.label()
    val description = stringResource(
        id = R.string.feature_applications_impl_status_chip_description,
        label,
    )
    HhApplicationStatusChip(
        kind = ApplicationStatusKindMapper().kindOf(status),
        label = label,
        modifier = modifier
            .heightIn(min = HhTheme.spacing.d48)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkspaceGapSectionPreview() {
    HhTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(HhTheme.spacing.d16)) {
            WorkspaceGapSection(
                gapCounts = WorkspaceGapCounts(met = 6, partial = 2, gap = 2),
                coverage = KeywordCoverage(covered = 10, total = 15),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkspacePrepTaskRowPreview() {
    HhTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(HhTheme.spacing.d16)) {
            WorkspacePrepTaskRow(
                task = WorkspacePrepTask(
                    id = "req-agile",
                    requirementText = "Agile delivery with JIRA",
                    priority = RequirementPriority.NICE_TO_HAVE,
                    isDone = false,
                    isOverflowOpen = true,
                ),
                onToggle = {},
                onOverflowToggle = {},
                onReport = {},
            )
        }
    }
}
