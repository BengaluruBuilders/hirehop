package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhOutlinedButton

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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "summary") {
            ReviewSummary(
                state = state,
                onAcceptAllSafeChanges = onAcceptAllSafeChanges,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        state.entries.forEach { entry ->
            item(key = "entry-${entry.entryId}") {
                EntryHeader(entry, Modifier.padding(horizontal = 16.dp))
            }
            items(items = entry.bullets, key = { it.bullet.id }) { bullet ->
                BulletCard(
                    item = bullet,
                    onAccept = { onAccept(bullet.bullet.id) },
                    onReject = { onReject(bullet.bullet.id) },
                    modifier = Modifier.padding(horizontal = 16.dp),
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_progress, state.reviewedCount, state.totalCount),
            style = MaterialTheme.typography.titleMedium,
        )
        LinearProgressIndicator(
            progress = { if (state.totalCount == 0) 1f else state.reviewedCount.toFloat() / state.totalCount },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_honesty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HhOutlinedButton(
            onClick = onAcceptAllSafeChanges,
            enabled = state.safeChangeBulletIds.isNotEmpty(),
            text = { Text(stringResource(R.string.feature_tailor_impl_accept_all)) },
        )
    }
}

@Composable
private fun EntryHeader(entry: TailorEntryUi, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
        if (entry.organization.isNotBlank()) {
            Text(
                text = entry.organization,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
