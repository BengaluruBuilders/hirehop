package com.hirehop.feature.profile.impl

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

internal fun CandidateProfile.unconfirmedCount(): Int = entries.count { !it.isConfirmed }

internal fun CandidateProfile.confirmEntry(entryId: String): CandidateProfile =
    replaceEntry(entryId) { it.copy(isConfirmed = true) }

internal fun CandidateProfile.confirmAll(): CandidateProfile =
    copy(entries = entries.map { it.copy(isConfirmed = true) })

internal fun CandidateProfile.deleteEntry(entryId: String): CandidateProfile =
    copy(entries = entries.filterNot { it.id == entryId })

internal fun CandidateProfile.upsertEntry(entry: ProfileEntry): CandidateProfile =
    if (entries.any { it.id == entry.id }) {
        replaceEntry(entry.id) { entry }
    } else {
        copy(entries = entries + entry)
    }

internal fun CandidateProfile.withContact(contact: ContactDraft): CandidateProfile = copy(
    fullName = contact.fullName.trim(),
    email = contact.email.trim(),
    phone = contact.phone.trim(),
    headline = contact.headline.trim(),
)

internal fun CandidateProfile.withSkill(skill: String): CandidateProfile {
    val trimmed = skill.trim()
    val isDuplicate = skills.any { it.equals(trimmed, ignoreCase = true) }
    return if (trimmed.isEmpty() || isDuplicate) this else copy(skills = skills + trimmed)
}

internal fun CandidateProfile.withoutSkill(skill: String): CandidateProfile =
    copy(skills = skills.filterNot { it.equals(skill, ignoreCase = true) })

internal fun CandidateProfile.asUnconfirmedImport(): CandidateProfile = copy(
    entries = entries.map { it.copy(source = FactSource.IMPORTED, isConfirmed = false) },
)

private fun CandidateProfile.replaceEntry(
    entryId: String,
    transform: (ProfileEntry) -> ProfileEntry,
): CandidateProfile = copy(
    entries = entries.map { if (it.id == entryId) transform(it) else it },
)
