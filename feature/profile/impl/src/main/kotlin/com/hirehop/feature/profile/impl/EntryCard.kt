package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.model.ProfileEntry

@Composable
internal fun EntryCard(
    entry: ProfileEntry,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EntryTitleRow(entry = entry)
            entry.bullets.forEach { bullet ->
                Text(
                    text = "• ${bullet.text}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            EntryActionRow(
                isConfirmed = entry.isConfirmed,
                onConfirm = onConfirm,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun EntryTitleRow(entry: ProfileEntry) {
    val dates = listOf(entry.startDate, entry.endDate)
        .filter { it.isNotBlank() }
        .let { parts ->
            if (parts.size == 2) {
                stringResource(R.string.feature_profile_impl_entry_dates, parts[0], parts[1])
            } else {
                parts.joinToString()
            }
        }
    val subtitle = listOf(entry.organization, dates).filter { it.isNotBlank() }.joinToString(" | ")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = entry.title, style = MaterialTheme.typography.titleSmall)
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!entry.isConfirmed) {
            UnconfirmedBadge()
        }
    }
}

@Composable
internal fun UnconfirmedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Text(
            text = stringResource(R.string.feature_profile_impl_unconfirmed_badge),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isConfirmed) {
            OutlinedButton(onClick = onConfirm) {
                Icon(imageVector = HhIcons.Check, contentDescription = null)
                Text(
                    text = stringResource(R.string.feature_profile_impl_confirm),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = HhIcons.Edit,
                    contentDescription = stringResource(R.string.feature_profile_impl_edit),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = HhIcons.Delete,
                    contentDescription = stringResource(R.string.feature_profile_impl_delete),
                )
            }
        }
    }
}

@Composable
internal fun AddEntryMenu(
    onAddEntry: (EntryTemplate) -> Unit,
    onPasteResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            TextButton(onClick = { expanded = true }) {
                Icon(imageVector = HhIcons.Add, contentDescription = null)
                Text(
                    text = stringResource(R.string.feature_profile_impl_add_entry),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            EntryTemplateMenu(
                expanded = expanded,
                onDismiss = { expanded = false },
                onSelect = {
                    expanded = false
                    onAddEntry(it)
                },
            )
        }
        TextButton(onClick = onPasteResume) {
            Text(stringResource(R.string.feature_profile_impl_paste_new_resume))
        }
    }
}

@Composable
private fun EntryTemplateMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelect: (EntryTemplate) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        EntryTemplate.entries.forEach { template ->
            DropdownMenuItem(
                text = { Text(stringResource(template.labelRes)) },
                onClick = { onSelect(template) },
            )
        }
    }
}
