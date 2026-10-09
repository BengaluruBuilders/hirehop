package com.tailormyresume.feature.profile.impl

import com.tailormyresume.core.model.CandidateProfile

internal fun CandidateProfile.unconfirmedCount(): Int = entries.count { !it.isConfirmed }

internal fun CandidateProfile.confirmEntry(entryId: String): CandidateProfile = copy(
    entries = entries.map { if (it.id == entryId) it.copy(isConfirmed = true) else it },
)

internal fun CandidateProfile.withContact(contact: ContactDraft): CandidateProfile = copy(
    fullName = contact.fullName.trim(),
    email = contact.email.trim(),
    phone = contact.phone.trim(),
    headline = contact.headline.trim(),
)

internal fun CandidateProfile.withSkill(skill: String): CandidateProfile {
    val trimmed = skill.trim()
    val isDuplicate = skills.any { it.equals(trimmed, ignoreCase = true) }
    return if (trimmed.isEmpty() || isDuplicate) this else copy(skills = skills + trimmed, userStatedSkills = userStatedSkills + trimmed)
}

internal fun CandidateProfile.withoutSkill(skill: String): CandidateProfile =
    copy(
        skills = skills.filterNot { it.equals(skill, ignoreCase = true) },
        userStatedSkills = userStatedSkills.filterNot { it.equals(skill, ignoreCase = true) },
    )

data class ContactDraft(
    val fullName: String,
    val email: String,
    val phone: String,
    val headline: String,
)

internal fun CandidateProfile.toContactDraft() = ContactDraft(
    fullName = fullName,
    email = email,
    phone = phone,
    headline = headline,
)
