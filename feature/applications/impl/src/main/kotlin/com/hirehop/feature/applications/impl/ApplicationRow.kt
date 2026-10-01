package com.hirehop.feature.applications.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.ui.ApplicationStatusKindMapper
import kotlin.time.Instant

private val ROW_TABULAR_FIGURES = "tnum"
private val ROW_MIN_HEIGHT: Dp = 56.dp
private val SYNC_RING_DIAMETER: Dp = 8.dp
private val SYNC_RING_STROKE: Dp = 1.5.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ApplicationRow(
    row: ApplicationListRow,
    now: Instant,
    onClick: () -> Unit,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = row.status.label()
    val syncLabel = stringResource(R.string.feature_applications_impl_sync_pending)
    val updatedSentence = updatedSentence(updatedAt = row.updatedAt, now = now)
    val chipDescription = stringResource(
        id = R.string.feature_applications_impl_status_chip_description,
        statusLabel,
    )
    val rowDescription = stringResource(
        id = R.string.feature_applications_impl_row_description,
        row.role,
        row.company,
        statusLabel,
        if (row.isSyncPending) syncLabel else coveragePhrase(row.coverage),
        updatedSentence,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT),
    ) {
        HhDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ROW_MIN_HEIGHT)
                .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.d12),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .semantics(mergeDescendants = true) { contentDescription = rowDescription },
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = row.role,
                    style = HhTheme.typography.titleMedium,
                    color = HhTheme.colors.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = row.company,
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                ) {
                    Text(
                        text = coverageShort(row.coverage),
                        style = HhTheme.typography.labelLarge.copy(
                            fontFeatureSettings = ROW_TABULAR_FIGURES,
                        ),
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                    if (row.isSyncPending) {
                        SyncPendingRing(label = syncLabel)
                    } else {
                        Text(
                            text = updatedSentence,
                            style = HhTheme.typography.bodySmall,
                            color = HhTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
            HhApplicationStatusChip(
                kind = ApplicationStatusKindMapper().kindOf(row.status),
                label = statusLabel,
                modifier = Modifier
                    .heightIn(min = HhTheme.spacing.d48)
                    .clickable(onClick = onStatusClick)
                    .semantics { contentDescription = chipDescription },
            )
        }
    }
}

@Composable
private fun SyncPendingRing(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.heightIn(min = HhTheme.spacing.d48),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Spacer(
            modifier = Modifier
                .size(SYNC_RING_DIAMETER)
                .dashedRing(),
        )
        Text(
            text = label,
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun Modifier.dashedRing(): Modifier {
    val ringColor = HhTheme.colors.onSurfaceVariant
    return drawBehind {
        val stroke = SYNC_RING_STROKE.toPx()
        val diameter = size.minDimension - stroke
        drawCircle(
            color = ringColor,
            radius = diameter / 2f,
            center = Offset(
                x = size.width / 2f,
                y = size.height / 2f,
            ),
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 2f, stroke * 2f)),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationRowPreview() {
    HhTheme(darkTheme = false) {
        Column {
            ApplicationRow(
                row = previewRow(id = "row-preview-1", isSyncPending = false),
                now = PREVIEW_INSTANT,
                onClick = {},
                onStatusClick = {},
            )
            ApplicationRow(
                row = previewRow(id = "row-preview-2", isSyncPending = true),
                now = PREVIEW_INSTANT,
                onClick = {},
                onStatusClick = {},
            )
        }
    }
}

private fun previewRow(
    id: String,
    isSyncPending: Boolean,
) = ApplicationListRow(
    id = id,
    role = "Associate Analyst",
    company = "Northwind GCC",
    status = ApplicationStatus.APPLIED,
    coverage = KeywordCoverage(covered = 9, total = 14),
    updatedAt = PREVIEW_INSTANT,
    isSyncPending = isSyncPending,
)
