package com.hirehop.feature.profile.impl

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.hirehop.core.model.EntryCategory

private const val NONE = "none"
private const val PASTE = "paste"
private const val CONTACT = "contact"
private const val SKILL = "skill"
private const val ENTRY = "entry"
private const val CONFIRM_ALL = "confirmAll"
private const val DELETE = "delete"
private const val CLEAR = "clear"
private const val DRAFT_FIXED_FIELDS = 5

internal val ProfileSheetSaver: Saver<ProfileSheet?, Any> = listSaver(
    save = { sheet -> sheet.encode() },
    restore = { parts -> parts.decodeSheet() },
)

internal val EntryDraftSaver: Saver<EntryDraft, Any> = listSaver(
    save = { draft ->
        listOf(draft.category.name, draft.title, draft.organization, draft.startDate, draft.endDate) +
            draft.bullets.flatMap { listOf(it.id.orEmpty(), it.text) }
    },
    restore = { parts ->
        EntryDraft(
            category = EntryCategory.valueOf(parts[0]),
            title = parts[1],
            organization = parts[2],
            startDate = parts[3],
            endDate = parts[4],
            bullets = parts.drop(DRAFT_FIXED_FIELDS).chunked(2).map {
                BulletDraft(id = it[0].ifEmpty { null }, text = it[1])
            },
        )
    },
)

internal val ContactDraftSaver: Saver<ContactDraft, Any> = listSaver(
    save = { listOf(it.fullName, it.email, it.phone, it.headline) },
    restore = { ContactDraft(fullName = it[0], email = it[1], phone = it[2], headline = it[3]) },
)

private fun ProfileSheet?.encode(): List<String> = when (this) {
    null -> listOf(NONE)
    ProfileSheet.PasteResume -> listOf(PASTE)
    ProfileSheet.EditContact -> listOf(CONTACT)
    ProfileSheet.AddSkill -> listOf(SKILL)
    ProfileSheet.ConfirmAll -> listOf(CONFIRM_ALL)
    ProfileSheet.ClearProfile -> listOf(CLEAR)
    is ProfileSheet.EditEntry -> listOf(ENTRY, entryId.orEmpty(), category.name)
    is ProfileSheet.DeleteEntry -> listOf(DELETE, entryId)
}

private fun List<String>.decodeSheet(): ProfileSheet? = when (first()) {
    PASTE -> ProfileSheet.PasteResume
    CONTACT -> ProfileSheet.EditContact
    SKILL -> ProfileSheet.AddSkill
    CONFIRM_ALL -> ProfileSheet.ConfirmAll
    CLEAR -> ProfileSheet.ClearProfile
    ENTRY -> ProfileSheet.EditEntry(
        entryId = get(1).ifEmpty { null },
        category = EntryCategory.valueOf(get(2)),
    )
    DELETE -> ProfileSheet.DeleteEntry(entryId = get(1))
    else -> null
}
