package com.hirehop.feature.profile.impl

import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

internal class EntryDraftMapper(private val idGenerator: IdGenerator) {

    fun toEntry(existing: ProfileEntry?, draft: EntryDraft): ProfileEntry {
        val saved = ProfileEntry(
            id = existing?.id ?: idGenerator.newId(),
            category = draft.category,
            title = draft.title.trim(),
            organization = draft.organization.trim(),
            startDate = draft.startDate.trim(),
            endDate = draft.endDate.trim(),
            bullets = draft.toBullets(),
            source = existing?.source ?: FactSource.USER_STATED,
            isConfirmed = true,
        )
        return if (existing != null && existing.isImportedAndChangedBy(saved)) {
            saved.copy(source = FactSource.USER_EDITED)
        } else {
            saved
        }
    }

    private fun EntryDraft.toBullets(): List<EvidenceBullet> = bullets
        .filter { it.text.isNotBlank() }
        .map { EvidenceBullet(id = it.id ?: idGenerator.newId(), text = it.text.trim()) }

    private fun ProfileEntry.isImportedAndChangedBy(saved: ProfileEntry): Boolean =
        source == FactSource.IMPORTED && saved.copy(source = source, isConfirmed = isConfirmed) != this
}
