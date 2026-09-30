package com.hirehop.feature.profile.impl

import androidx.compose.runtime.Composable
import com.hirehop.core.model.EntryCategory

internal sealed interface ProfileSheet {
    data object PasteResume : ProfileSheet

    data object EditContact : ProfileSheet

    data object AddSkill : ProfileSheet

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
    val profile = (uiState as? ProfileUiState.Success)?.profile
    when (sheet) {
        null -> Unit

        ProfileSheet.PasteResume -> PasteResumeSheet(
            state = importState,
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

        ProfileSheet.EditContact -> if (profile != null) {
            ContactEditorDialog(
                initial = ContactDraft(profile.fullName, profile.email, profile.phone, profile.headline),
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

        is ProfileSheet.EditEntry -> if (profile != null) {
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
    }
}
