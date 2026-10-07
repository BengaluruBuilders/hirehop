package com.hirehop.feature.applications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhCoverageBar
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
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
    val changeStatusAction = CustomAccessibilityAction(label = changeStatus) {
        onStatusClick()
        true
    }
    val rowModifier = modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) {
            contentDescription = rowDescription
            customActions = listOf(changeStatusAction)
        }
    val coverageLine = stringResource(
        R.string.feature_applications_impl_coverage_key_terms,
        row.coverage.covered,
        row.coverage.total,
    )
    HhPillRow(
        title = role,
        onClick = onClick,
        style = HhPillRowStyle.Neutral,
        subtitle = stringResource(R.string.feature_applications_impl_row_company_updated, company, updatedLabel),
        monogram = monogramOf(row.company),
        titleMaxLines = 2,
        modifier = rowModifier,
    ) {
        HhCoverageBar(
            met = row.coverage.covered,
            partial = 0,
            gap = row.coverage.total - row.coverage.covered,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            HhApplicationStatusChip(
                kind = ApplicationStatusKindMapper().kindOf(row.status),
                label = statusLabel,
                modifier = Modifier
                    .defaultMinSize(minHeight = HhTheme.spacing.touch)
                    .clip(HhTheme.shapes.pill)
                    .clickable(onClick = onStatusClick),
            )
            Text(text = coverageLine, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
            if (row.isSyncPending) SyncPendingChip()
        }
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
    HhTheme(darkTheme = false) { ApplicationRowPreviewRows() }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationRowDarkPreview() {
    HhTheme(darkTheme = true) { ApplicationRowPreviewRows() }
}

@Composable
private fun ApplicationRowPreviewRows() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        ApplicationRow(
            row = previewRow(id = "row-preview-1", isSyncPending = false, isExported = true),
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
        ApplicationRow(
            row = previewRow(
                id = "row-preview-3",
                isSyncPending = false,
                status = ApplicationStatus.INTERVIEW,
            ),
            now = PREVIEW_INSTANT,
            onClick = {},
            onStatusClick = {},
        )
        ApplicationRow(
            row = previewRow(
                id = "row-preview-4",
                isSyncPending = true,
                status = ApplicationStatus.OFFER,
            ),
            now = PREVIEW_INSTANT,
            onClick = {},
            onStatusClick = {},
        )
    }
}

private fun previewRow(
    id: String,
    isSyncPending: Boolean,
    status: ApplicationStatus = ApplicationStatus.APPLIED,
    isExported: Boolean = false,
) = ApplicationListRow(
    id = id,
    role = "Associate Analyst",
    company = "Northwind GCC",
    status = status,
    coverage = KeywordCoverage(covered = 9, total = 14),
    updatedAt = PREVIEW_INSTANT,
    isSyncPending = isSyncPending,
    isExported = isExported,
)
