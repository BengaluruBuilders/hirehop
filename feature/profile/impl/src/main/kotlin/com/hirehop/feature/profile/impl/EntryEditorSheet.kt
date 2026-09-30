package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.model.EntryCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntryEditorSheet(
    initial: EntryDraft,
    isNew: Boolean,
    onSave: (EntryDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by rememberSaveable(stateSaver = EntryDraftSaver) { mutableStateOf(initial) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EditorHeading(isNew = isNew)
            CategoryChips(selected = draft.category, onSelect = { draft = draft.copy(category = it) })
            EntryFields(draft = draft, onChange = { draft = it })
            BulletFields(draft = draft, onChange = { draft = it })
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.feature_profile_impl_cancel))
                }
                HhButton(
                    onClick = { onSave(draft) },
                    enabled = draft.canSave,
                    text = { Text(stringResource(R.string.feature_profile_impl_save)) },
                )
            }
        }
    }
}

@Composable
private fun EditorHeading(isNew: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(
                if (isNew) {
                    R.string.feature_profile_impl_editor_add_title
                } else {
                    R.string.feature_profile_impl_editor_edit_title
                },
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        if (!isNew) {
            Text(
                text = stringResource(R.string.feature_profile_impl_editor_edit_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    selected: EntryCategory,
    onSelect: (EntryCategory) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EntryCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(stringResource(category.labelRes())) },
            )
        }
    }
}

@Composable
private fun EntryFields(
    draft: EntryDraft,
    onChange: (EntryDraft) -> Unit,
) {
    OutlinedTextField(
        value = draft.title,
        onValueChange = { onChange(draft.copy(title = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.feature_profile_impl_field_title)) },
        singleLine = true,
    )
    OutlinedTextField(
        value = draft.organization,
        onValueChange = { onChange(draft.copy(organization = it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.feature_profile_impl_field_organization)) },
        singleLine = true,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = draft.startDate,
            onValueChange = { onChange(draft.copy(startDate = it)) },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.feature_profile_impl_field_start)) },
            singleLine = true,
        )
        OutlinedTextField(
            value = draft.endDate,
            onValueChange = { onChange(draft.copy(endDate = it)) },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.feature_profile_impl_field_end)) },
            singleLine = true,
        )
    }
}

@Composable
private fun BulletFields(
    draft: EntryDraft,
    onChange: (EntryDraft) -> Unit,
) {
    draft.bullets.forEachIndexed { index, bullet ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = bullet.text,
                onValueChange = { onChange(draft.withBulletText(index, it)) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.feature_profile_impl_field_bullet, index + 1)) },
                minLines = 2,
            )
            IconButton(onClick = { onChange(draft.withoutBullet(index)) }) {
                Icon(
                    imageVector = HhIcons.Close,
                    contentDescription = stringResource(R.string.feature_profile_impl_remove_bullet, index + 1),
                )
            }
        }
    }
    TextButton(onClick = { onChange(draft.withNewBullet()) }) {
        Icon(imageVector = HhIcons.Add, contentDescription = null)
        Text(
            text = stringResource(R.string.feature_profile_impl_add_bullet),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
