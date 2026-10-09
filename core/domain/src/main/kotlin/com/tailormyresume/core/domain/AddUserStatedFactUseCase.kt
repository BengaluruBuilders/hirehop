package com.tailormyresume.core.domain

import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ProfileLimits
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddUserStatedFactUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val idGenerator: IdGenerator,
) {
    private val idAllocator = FactIdAllocator()

    suspend operator fun invoke(requirement: JobRequirement, statement: String) {
        val updated = preview(requirement, statement) ?: return
        profileRepository.saveProfile(updated)
    }

    suspend fun preview(requirement: JobRequirement, statement: String): CandidateProfile? {
        val trimmed = statement.trim()
        if (trimmed.isEmpty()) return null
        val profile = profileRepository.observeProfile().first() ?: return null
        val stated = keywordsStatedIn(requirement, trimmed)
        return profile.copy(skills = mergedSkills(profile.skills, stated)).withStatement(trimmed)
    }

    fun nextFactId(profile: CandidateProfile): String =
        profile.userStatedTarget()?.let { FactDisplayIds.of(it, profile.entries) }
            ?: idAllocator.nextUncategorisedId(profile.entries)

    private fun CandidateProfile.userStatedTarget(): ProfileEntry? =
        entries.lastOrNull { it.isUserStatedCollection() && it.bullets.size < ProfileLimits.MAX_BULLETS_PER_ENTRY }

    private fun mergedSkills(existing: List<String>, stated: List<String>): List<String> {
        val known = existing.map { it.lowercase() }.toMutableSet()
        return existing + stated.filter { known.add(it.lowercase()) }
    }

    private fun CandidateProfile.withStatement(statement: String): CandidateProfile {
        val bullet = EvidenceBullet(id = idGenerator.newId(), text = statement)
        val target = userStatedTarget()
        val updated = if (target != null) {
            entries.map { if (it === target) it.copy(bullets = it.bullets + bullet) else it }
        } else {
            entries + newEntry(bullet, entries)
        }
        return copy(entries = updated)
    }

    private fun ProfileEntry.isUserStatedCollection(): Boolean =
        source == FactSource.USER_STATED && title == USER_STATED_ENTRY_TITLE

    private fun newEntry(bullet: EvidenceBullet, existing: List<ProfileEntry>) = ProfileEntry(
        id = idAllocator.nextUncategorisedId(existing),
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
        const val USER_STATED_ENTRY_TITLE = "Additional experience"
    }
}
