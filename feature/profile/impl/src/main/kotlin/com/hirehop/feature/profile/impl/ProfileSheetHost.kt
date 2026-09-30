package com.hirehop.feature.profile.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory

internal sealed interface ProfileSheet {
    data object PasteResume : ProfileSheet

    data object EditContact : ProfileSheet

    data object AddSkill : ProfileSheet

    data object ConfirmAll : ProfileSheet

    data object ClearProfile : ProfileSheet

    data class DeleteEntry(val entryId: String) : ProfileSheet

    data class EditEntry(
        val entryId: String?,
        val category: EntryCategory,
    ) : ProfileSheet
}

@Composable
internal fun ProfileSheetHost(
    sheet: ProfileSheet?,
    uiState: ProfileUiState,
    importState: ResumeImportState,
    actions: ProfileActions,
    onDismiss: () -> Unit,
) {
    val success = uiState as? ProfileUiState.Success
    when (sheet) {
        null -> Unit

        ProfileSheet.PasteResume -> PasteResumeSheet(
            state = importState,
            hasExistingProfile = success != null,
            onTextChange = actions.onResumeTextChange,
            onParse = actions.onParseResume,
            onSave = {
                actions.onSavePreview()
                onDismiss()
            },
            onDismiss = {
                actions.onResetImport()
                onDismiss()
            },
        )

        ProfileSheet.EditContact -> if (success != null) {
            ContactEditorDialog(
                initial = success.profile.toContactDraft(),
                onSave = {
                    actions.onUpdateContact(it)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        ProfileSheet.AddSkill -> SkillInputDialog(
            onAdd = {
                actions.onAddSkill(it)
                onDismiss()
            },
            onDismiss = onDismiss,
        )

        is ProfileSheet.EditEntry -> if (success != null) {
            EntryEditorHost(sheet, success.profile, actions, onDismiss)
        }

        else -> ConfirmationSheetHost(sheet, success, actions, onDismiss)
    }
}

@Composable
private fun EntryEditorHost(
    sheet: ProfileSheet.EditEntry,
    profile: CandidateProfile,
    actions: ProfileActions,
    onDismiss: () -> Unit,
) {
    val entry = profile.entries.firstOrNull { it.id == sheet.entryId }
    EntryEditorSheet(
        initial = entry?.toDraft() ?: EntryDraft.blank(sheet.category),
        isNew = entry == null,
        onSave = {
            actions.onSaveEntry(sheet.entryId, it)
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun ConfirmationSheetHost(
    sheet: ProfileSheet,
    success: ProfileUiState.Success?,
    actions: ProfileActions,
    onDismiss: () -> Unit,
) {
    when (sheet) {
        ProfileSheet.ConfirmAll -> if (success != null) {
            ConfirmationDialog(
                title = pluralStringResource(
                    R.plurals.feature_profile_impl_confirm_all_title,
                    success.unconfirmedCount,
                    success.unconfirmedCount,
                ),
                message = stringResource(R.string.feature_profile_impl_confirm_all_message),
                confirmLabel = stringResource(R.string.feature_profile_impl_confirm),
                onConfirm = {
                    actions.onConfirmAll()
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        is ProfileSheet.DeleteEntry -> ConfirmationDialog(
            title = stringResource(R.string.feature_profile_impl_delete_title),
            message = stringResource(R.string.feature_profile_impl_delete_message),
            confirmLabel = stringResource(R.string.feature_profile_impl_delete),
            onConfirm = {
                actions.onDeleteEntry(sheet.entryId)
                onDismiss()
            },
            onDismiss = onDismiss,
        )

        ProfileSheet.ClearProfile -> ConfirmationDialog(
            title = stringResource(R.string.feature_profile_impl_clear_title),
            message = stringResource(R.string.feature_profile_impl_clear_message),
            confirmLabel = stringResource(R.string.feature_profile_impl_clear_confirm),
            onConfirm = {
                actions.onClearProfile()
                onDismiss()
            },
            onDismiss = onDismiss,
        )

        else -> Unit
    }
}

private fun CandidateProfile.toContactDraft() = ContactDraft(
    fullName = fullName,
    email = email,
    phone = phone,
    headline = headline,
)
