package com.hirehop.feature.applications.impl

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhMonogram
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.ui.ApplicationStatusKindMapper
import kotlin.time.Instant

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
    val updatedLabel = updatedSentence(updatedAt = row.updatedAt, now = now)
    val changeStatus = stringResource(R.string.feature_applications_impl_row_change_status)
    val role = roleOrFallback(row.role)
    val company = companyOrFallback(row.company)
    val rowDescription = stringResource(
        id = R.string.feature_applications_impl_row_description,
        role,
        company,
        statusLabel,
        coveragePhrase(row.coverage),
        updatedLabel,
    )
    HhCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = rowDescription
                customActions = listOf(
                    CustomAccessibilityAction(label = changeStatus) {
                        onStatusClick()
                        true
                    },
                )
            },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            HhMonogram(text = monogramOf(row.company))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                    Text(
                        text = role,
                        style = HhTheme.typography.titleM,
                        color = HhTheme.colors.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            id = R.string.feature_applications_impl_row_company_updated,
                            company,
                            updatedLabel,
                        ),
                        style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Normal),
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HhApplicationStatusChip(
                            kind = ApplicationStatusKindMapper().kindOf(row.status),
                            label = statusLabel,
                            modifier = Modifier
                                .clip(HhTheme.shapes.pill)
                                .clickable(onClick = onStatusClick),
                        )
                        if (row.isSyncPending) SyncPendingChip()
                    }
                    CoverageFigure(coverage = row.coverage)
                }
            }
        }
    }
}

@Composable
private fun CoverageFigure(
    coverage: KeywordCoverage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = coverageFraction(coverage),
            style = HhTheme.typography.numeralM.copy(
                fontSize = HhTheme.typography.labelL.fontSize,
                lineHeight = HhTheme.typography.labelL.lineHeight,
            ),
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_applications_impl_key_terms),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SyncPendingChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = HhTheme.spacing.d24)
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.neutralContainer)
            .padding(horizontal = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = HhIcons.OfflineCloud,
            contentDescription = null,
            tint = HhTheme.colors.onNeutralContainer,
            modifier = Modifier.size(HhTheme.spacing.lg),
        )
        Text(
            text = stringResource(R.string.feature_applications_impl_sync_pending),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationRowPreview() {
    HhTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
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
