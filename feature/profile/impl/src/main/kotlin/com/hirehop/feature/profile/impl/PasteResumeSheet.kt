package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ProfileEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PasteResumeSheet(
    state: ResumeImportState,
    hasExistingProfile: Boolean,
    onTextChange: (String) -> Unit,
    onParse: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmingReplace by rememberSaveable { mutableStateOf(false) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_profile_impl_paste_title),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = state.rawText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.feature_profile_impl_paste_field)) },
                minLines = 6,
                maxLines = 12,
            )
            Text(
                text = stringResource(R.string.feature_profile_impl_paste_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ParseButton(state = state, onParse = onParse)
            state.preview?.let { preview ->
                ResumePreview(
                    preview = preview,
                    hasContent = state.previewHasContent,
                    onSave = { if (hasExistingProfile) confirmingReplace = true else onSave() },
                )
            }
        }
    }
    if (confirmingReplace) {
        ConfirmationDialog(
            title = stringResource(R.string.feature_profile_impl_replace_title),
            message = stringResource(R.string.feature_profile_impl_replace_message),
            confirmLabel = stringResource(R.string.feature_profile_impl_replace_confirm),
            onConfirm = {
                confirmingReplace = false
                onSave()
            },
            onDismiss = { confirmingReplace = false },
        )
    }
}

@Composable
private fun ParseButton(
    state: ResumeImportState,
    onParse: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhOutlinedButton(
            onClick = onParse,
            enabled = state.canParse,
            text = { Text(stringResource(R.string.feature_profile_impl_paste_parse)) },
        )
        if (state.isParsing) {
            HhLoadingWheel(
                contentDesc = stringResource(R.string.feature_profile_impl_paste_parsing),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ResumePreview(
    preview: CandidateProfile,
    hasContent: Boolean,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.feature_profile_impl_paste_preview_title),
            style = MaterialTheme.typography.titleMedium,
        )
        if (hasContent) {
            PreviewSummary(preview)
            Text(
                text = stringResource(R.string.feature_profile_impl_paste_unconfirmed_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HhButton(
                onClick = onSave,
                text = { Text(stringResource(R.string.feature_profile_impl_paste_save)) },
            )
        } else {
            Text(
                text = stringResource(R.string.feature_profile_impl_paste_nothing_found),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PreviewSummary(preview: CandidateProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val contactLine = listOf(preview.fullName, preview.email, preview.phone)
            .filter { it.isNotBlank() }
            .joinToString(separator = "  |  ")
        if (contactLine.isNotEmpty()) {
            Text(text = contactLine, style = MaterialTheme.typography.bodyLarge)
        }
        if (preview.skills.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.feature_profile_impl_paste_skills,
                    preview.skills.joinToString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        preview.entries.forEach { PreviewEntryRow(it) }
    }
}

@Composable
private fun PreviewEntryRow(entry: ProfileEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = entry.title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = listOf(
                stringResource(entry.category.labelRes()),
                entry.organization,
                pluralStringResource(
                    R.plurals.feature_profile_impl_paste_entry_bullets,
                    entry.bullets.size,
                    entry.bullets.size,
                ),
            ).filter { it.isNotBlank() }.joinToString(" | "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
