package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhSegmentedCounter
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun ReviewContent(
    state: TailorUiState.Success,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onAcceptAllSafeChanges: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        item(key = "summary") {
            ReviewSummary(
                state = state,
                onAcceptAllSafeChanges = onAcceptAllSafeChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HhTheme.spacing.lg),
            )
        }
        state.entries.forEach { entry ->
            item(key = "entry-${entry.entryId}") {
                EntryHeader(
                    entry = entry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HhTheme.spacing.lg),
                )
            }
            items(items = entry.bullets, key = { it.bullet.id }) { bullet ->
                BulletCard(
                    item = bullet,
                    onAccept = { onAccept(bullet.bullet.id) },
                    onReject = { onReject(bullet.bullet.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HhTheme.spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun ReviewSummary(
    state: TailorUiState.Success,
    onAcceptAllSafeChanges: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val spacing = HhTheme.spacing
    val flaggedCount = state.flaggedBulletCount
    HhSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            HhSegmentedCounter(current = state.reviewedCount, total = state.totalCount)
            Text(
                text = stringResource(R.string.feature_tailor_impl_changes_reviewed),
                style = HhTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        if (flaggedCount > 0) {
            HhStatusChip(
                kind = HhStatusKind.Partial,
                label = pluralStringResource(
                    id = R.plurals.feature_tailor_impl_flagged,
                    count = flaggedCount,
                    flaggedCount,
                ),
            )
        }
        HhDivider()
        Text(
            text = stringResource(R.string.feature_tailor_impl_honesty),
            style = HhTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        HhOutlinedButton(
            onClick = onAcceptAllSafeChanges,
            enabled = state.safeChangeBulletIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            text = { Text(stringResource(R.string.feature_tailor_impl_accept_all)) },
        )
    }
}

@Composable
private fun EntryHeader(entry: TailorEntryUi, modifier: Modifier = Modifier) {
    val colors = HhTheme.colors
    Column(modifier = modifier.padding(top = HhTheme.spacing.sm)) {
        Text(
            text = entry.title,
            style = HhTheme.typography.titleMedium,
            color = colors.onSurface,
        )
        if (entry.organization.isNotBlank()) {
            Text(
                text = entry.organization,
                style = HhTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        HhDivider()
    }
}

private val TailorUiState.Success.flaggedBulletCount: Int
    get() = entries.sumOf { entry -> entry.bullets.count { it.kind == BulletReviewKind.VIOLATION } }
