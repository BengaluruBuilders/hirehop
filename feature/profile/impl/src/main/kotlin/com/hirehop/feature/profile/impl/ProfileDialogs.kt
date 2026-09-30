package com.hirehop.feature.profile.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
internal fun ContactEditorDialog(
    initial: ContactDraft,
    onSave: (ContactDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by rememberSaveable(stateSaver = ContactDraftSaver) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_profile_impl_contact_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ContactField(
                    label = R.string.feature_profile_impl_field_full_name,
                    value = draft.fullName,
                    onValueChange = { draft = draft.copy(fullName = it) },
                )
                ContactField(
                    label = R.string.feature_profile_impl_field_headline,
                    value = draft.headline,
                    onValueChange = { draft = draft.copy(headline = it) },
                )
                ContactField(
                    label = R.string.feature_profile_impl_field_email,
                    value = draft.email,
                    onValueChange = { draft = draft.copy(email = it) },
                    keyboardType = KeyboardType.Email,
                )
                ContactField(
                    label = R.string.feature_profile_impl_field_phone,
                    value = draft.phone,
                    onValueChange = { draft = draft.copy(phone = it) },
                    keyboardType = KeyboardType.Phone,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) {
                Text(stringResource(R.string.feature_profile_impl_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_profile_impl_cancel))
            }
        },
    )
}

@Composable
private fun ContactField(
    @StringRes label: Int,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
internal fun SkillInputDialog(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var skill by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_profile_impl_add_skill)) },
        text = {
            OutlinedTextField(
                value = skill,
                onValueChange = { skill = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.feature_profile_impl_skill_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(skill) }, enabled = skill.isNotBlank()) {
                Text(stringResource(R.string.feature_profile_impl_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_profile_impl_cancel))
            }
        },
    )
}

@Composable
internal fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_profile_impl_cancel))
            }
        },
    )
}
