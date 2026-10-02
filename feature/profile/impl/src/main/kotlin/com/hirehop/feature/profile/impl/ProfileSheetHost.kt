package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhSheetActionRow
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory

internal enum class ProfileSheet { Contact, AddSkill, AddFact }

@Composable
internal fun ProfileSheetHost(
    sheet: ProfileSheet?,
    profile: CandidateProfile,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    onDismiss: () -> Unit,
) {
    when (sheet) {
        null -> Unit

        ProfileSheet.Contact -> ContactSheet(
            initial = profile.toContactDraft(),
            onSave = {
                actions.onUpdateContact(it)
                onDismiss()
            },
            onDismiss = onDismiss,
        )

        ProfileSheet.AddSkill -> AddSkillSheet(
            onAdd = {
                actions.onAddSkill(it)
                onDismiss()
            },
            onDismiss = onDismiss,
        )

        ProfileSheet.AddFact -> AddFactSheet(
            onPick = {
                onDismiss()
                navigation.onAddFact(it.name)
            },
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactSheet(
    initial: ContactDraft,
    onSave: (ContactDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    var fullName by rememberSaveable { mutableStateOf(initial.fullName) }
    var email by rememberSaveable { mutableStateOf(initial.email) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var headline by rememberSaveable { mutableStateOf(initial.headline) }
    HhBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.feature_profile_impl_contact_title),
        subtitle = stringResource(R.string.feature_profile_impl_contact_subtitle),
    ) {
        HhTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = stringResource(R.string.feature_profile_impl_field_full_name),
        )
        HhTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.feature_profile_impl_field_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        HhTextField(
            value = phone,
            onValueChange = { phone = it },
            label = stringResource(R.string.feature_profile_impl_field_phone),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        HhTextField(
            value = headline,
            onValueChange = { headline = it },
            label = stringResource(R.string.feature_profile_impl_field_headline),
        )
        SheetActions(
            confirmLabel = stringResource(R.string.feature_profile_impl_save),
            onConfirm = { onSave(ContactDraft(fullName, email, phone, headline)) },
            onCancel = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddSkillSheet(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var skill by rememberSaveable { mutableStateOf("") }
    HhBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.feature_profile_impl_add_skill),
    ) {
        HhTextField(
            value = skill,
            onValueChange = { skill = it },
            label = stringResource(R.string.feature_profile_impl_skill_label),
            placeholder = stringResource(R.string.feature_profile_impl_skill_placeholder),
        )
        SheetActions(
            confirmLabel = stringResource(R.string.feature_profile_impl_add),
            onConfirm = { onAdd(skill) },
            onCancel = onDismiss,
            confirmEnabled = skill.isNotBlank(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFactSheet(
    onPick: (EntryCategory) -> Unit,
    onDismiss: () -> Unit,
) {
    HhBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.feature_profile_impl_add_fact_title),
    ) {
        EntryCategory.entries.forEach { category ->
            HhSheetActionRow(
                icon = HhIcons.Add,
                title = stringResource(category.addTitleRes()),
                onClick = { onPick(category) },
            )
        }
    }
}

@Composable
private fun SheetActions(
    confirmLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhOutlineButton(
            label = stringResource(R.string.feature_profile_impl_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = confirmLabel,
            onClick = onConfirm,
            enabled = confirmEnabled,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun EntryCategory.addTitleRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_add_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_add_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_add_project
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_add_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_add_achievement
}
