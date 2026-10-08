package com.tailormyresume.core.domain

import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.fact.AddFactsOutcome
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftValidator
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddUserStatedFactsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val idGenerator: IdGenerator,
) {
    private val idAllocator = FactIdAllocator()

    suspend operator fun invoke(facts: List<FactDraft>): AddFactsOutcome {
        val drafts = facts.filter { it.hasContent() }
        if (drafts.isEmpty()) return AddFactsOutcome.NothingToAdd
        val errors = drafts.flatMap { FactDraftValidator.validate(it) }
        if (errors.isNotEmpty()) return AddFactsOutcome.Rejected(errors)
        val profile = profileRepository.observeProfile().first() ?: blankProfile()
        val merged = drafts.fold(profile.entries) { entries, draft -> entries + newEntry(draft, entries) }
        profileRepository.saveProfile(profile.copy(entries = merged))
        return AddFactsOutcome.Added(merged.drop(profile.entries.size))
    }

    private fun newEntry(draft: FactDraft, existing: List<ProfileEntry>): ProfileEntry {
        val detail = draft.detail.trim()
        val bullet = if (detail.isEmpty()) null else EvidenceBullet(id = idGenerator.newId(), text = detail)
        return ProfileEntry(
            id = idAllocator.nextId(draft.category, existing, draft.title),
            category = draft.category,
            title = draft.title.trim(),
            organization = draft.organization.trim(),
            startDate = draft.startDate.trim(),
            endDate = draft.endDate.trim(),
            bullets = listOfNotNull(bullet),
            source = FactSource.USER_STATED,
            isConfirmed = true,
        )
    }

    private fun blankProfile() = CandidateProfile(
        fullName = "",
        email = "",
        phone = "",
        headline = "",
        skills = emptyList(),
        entries = emptyList(),
    )

    private fun FactDraft.hasContent(): Boolean =
        title.isNotBlank() ||
            organization.isNotBlank() ||
            startDate.isNotBlank() ||
            endDate.isNotBlank() ||
            detail.isNotBlank()
}
