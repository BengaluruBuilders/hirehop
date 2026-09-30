package com.hirehop.core.domain

import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.ProfileEntry
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddUserStatedFactUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val idGenerator: IdGenerator,
) {
    suspend operator fun invoke(requirement: JobRequirement, statement: String) {
        val profile = profileRepository.observeProfile().first() ?: return
        val withSkills = profile.copy(skills = mergedSkills(profile.skills, requirement.keywords))
        profileRepository.saveProfile(withSkills.withStatement(statement.trim()))
    }

    private fun mergedSkills(existing: List<String>, keywords: List<String>): List<String> {
        val known = existing.map { it.lowercase() }.toMutableSet()
        return existing + keywords.filter { known.add(it.lowercase()) }
    }

    private fun CandidateProfile.withStatement(statement: String): CandidateProfile {
        if (statement.isEmpty()) return this
        val bullet = EvidenceBullet(id = idGenerator.newId(), text = statement)
        val exists = entries.any { it.id == USER_STATED_ENTRY_ID }
        val updated = if (exists) entries.map { it.appendingTo(bullet) } else entries + newEntry(bullet)
        return copy(entries = updated)
    }

    private fun ProfileEntry.appendingTo(bullet: EvidenceBullet): ProfileEntry =
        if (id == USER_STATED_ENTRY_ID) copy(bullets = bullets + bullet) else this

    private fun newEntry(bullet: EvidenceBullet) = ProfileEntry(
        id = USER_STATED_ENTRY_ID,
        category = EntryCategory.ACHIEVEMENT,
        title = USER_STATED_ENTRY_TITLE,
        organization = "",
        startDate = "",
        endDate = "",
        bullets = listOf(bullet),
        source = FactSource.USER_STATED,
        isConfirmed = true,
    )

    private companion object {
        const val USER_STATED_ENTRY_ID = "user-stated"
        const val USER_STATED_ENTRY_TITLE = "Additional experience"
    }
}
