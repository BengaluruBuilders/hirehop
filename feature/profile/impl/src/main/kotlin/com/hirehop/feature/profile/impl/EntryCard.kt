package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.ui.FactIdTag

@Composable
internal fun EntryCard(
    entry: ProfileEntry,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateLabel = if (entry.isConfirmed) {
        stringResource(R.string.feature_profile_impl_fact_confirmed)
    } else {
        stringResource(R.string.feature_profile_impl_fact_not_confirmed)
    }
    val announcement = stringResource(
        R.string.feature_profile_impl_fact_card_accessibility,
        stringResource(entry.category.factKindRes()),
        entry.title,
        stateLabel,
        entry.id,
        stringResource(R.string.feature_profile_impl_fact_edit_action),
    )
    HhCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(HhTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = announcement },
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = entry.title,
                    style = HhTheme.typography.titleMedium,
                    color = HhTheme.colors.onSurface,
                )
                val subtitle = listOf(entry.organization, entry.dateSpan())
                    .filter { it.isNotBlank() }
                    .joinToString(separator = "  |  ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = HhTheme.typography.bodySmall,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
                FactProvenanceRow(entry = entry)
            }
            FactIdTag(factId = entry.id)
        }
        EntryActionRow(
            isConfirmed = entry.isConfirmed,
            onConfirm = onConfirm,
            onEdit = onEdit,
            onDelete = onDelete,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactProvenanceRow(entry: ProfileEntry) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhProvenanceChip(kind = entry.source.provenanceKind(isConfirmed = entry.isConfirmed))
        if (!entry.isConfirmed) {
            HhStatusChip(
                kind = HhStatusKind.Partial,
                label = stringResource(R.string.feature_profile_impl_fact_not_confirmed),
            )
        }
    }
}

@Composable
private fun ProfileEntry.dateSpan(): String {
    val parts = listOf(startDate, endDate).filter { it.isNotBlank() }
    return when (parts.size) {
        2 -> stringResource(
            R.string.feature_profile_impl_entry_dates,
            parts[0],
            parts[1],
        )

        else -> parts.joinToString(separator = " ")
    }
}

@Composable
private fun EntryActionRow(
    isConfirmed: Boolean,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isConfirmed) {
            HhOutlinedButton(
                onClick = onConfirm,
                modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_profile_impl_confirm),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        }
        HhOutlinedButton(
            onClick = onEdit,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_profile_impl_edit),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
        HhOutlinedButton(
            onClick = onDelete,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_profile_impl_delete),
                    style = HhTheme.typography.labelLarge,
                    color = HhTheme.colors.error,
                )
            },
        )
    }
}

@Composable
internal fun AddEntryMenu(
    onAddEntry: (EntryTemplate) -> Unit,
    onPasteResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            HhOutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_profile_impl_add_entry),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                EntryTemplate.entries.forEach { template ->
                    DropdownMenuItem(
                        text = { Text(stringResource(template.labelRes)) },
                        onClick = {
                            expanded = false
                            onAddEntry(template)
                        },
                    )
                }
            }
        }
        HhOutlinedButton(
            onClick = onPasteResume,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_profile_impl_paste_new_resume),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}
