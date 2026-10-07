package com.hirehop.feature.applications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
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
    val coverageLine = coverageLine(row.coverage)
    HhCard(
        modifier = rowModifier,
        contentPadding = PaddingValues(HhTheme.spacing.md),
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            LogoTile(company = row.company)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            ) {
                Column {
                    Text(
                        text = role,
                        style = HhTheme.typography.titleL,
                        color = HhTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.feature_applications_impl_row_company_updated,
                            company,
                            updatedLabel,
                        ),
                        style = HhTheme.typography.labelM,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusChip(status = row.status, label = statusLabel, onClick = onStatusClick)
                    if (row.isSyncPending) SyncPendingChip()
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = HhIcons.CheckCircle,
                        contentDescription = null,
                        tint = HhTheme.colors.met,
                        modifier = Modifier.size(HhTheme.spacing.lg),
                    )
                    Text(
                        text = coverageLine,
                        style = HhTheme.typography.labelM,
                        color = HhTheme.colors.onSurface,
                    )
                }
            }
        }
    }
}

private val RowGap = 14.dp
private val LogoTileSize = 44.dp
private val LogoTileShape = RoundedCornerShape(14.dp)

@Composable
internal fun LogoTile(company: String, modifier: Modifier = Modifier, size: Dp = LogoTileSize) {
    val tiles = HhTheme.colors.logoTiles
    val fill = tiles[company.trim().lowercase().hashCode().mod(tiles.size)]
    Box(
        modifier = modifier
            .size(size)
            .clip(LogoTileShape)
            .background(fill)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogramOf(company).trim().take(1).uppercase(),
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onLogoTile,
        )
    }
}

@Composable
internal fun StatusChip(
    status: ApplicationStatus,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val tint = when (status) {
        ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER -> colors.met
        ApplicationStatus.REJECTED, ApplicationStatus.NO_RESPONSE -> colors.onSurfaceVariant
        else -> colors.onSurface
    }
    Box(
        modifier = modifier
            .heightIn(min = HhTheme.spacing.touch)
            .clip(HhTheme.shapes.pill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = ChipHeight)
                .clip(HhTheme.shapes.pill)
                .background(colors.primaryContainer)
                .padding(start = ChipStartPadding, end = HhTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = status.glyph(),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(HhTheme.spacing.lg),
            )
            Text(
                text = label,
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
                color = tint,
            )
        }
    }
}

private val ChipHeight = 26.dp
private val ChipStartPadding = 7.dp

private fun ApplicationStatus.glyph(): ImageVector = when (this) {
    ApplicationStatus.SAVED -> HhIcons.Bookmark
    ApplicationStatus.APPLIED -> HhIcons.Send
    ApplicationStatus.INTERVIEW -> HhIcons.Calendar
    ApplicationStatus.OFFER -> HhIcons.Award
    ApplicationStatus.REJECTED -> HhIcons.CancelCircle
    ApplicationStatus.NO_RESPONSE -> HhIcons.Clock
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
