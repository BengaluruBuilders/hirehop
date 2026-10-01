package com.hirehop.core.domain

import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.fact.AddFactsOutcome
import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
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
        val profile = profileRepository.observeProfile().first() ?: return AddFactsOutcome.NothingToAdd
        val merged = drafts.fold(profile.entries) { entries, draft -> entries + newEntry(draft, entries) }
        profileRepository.saveProfile(profile.copy(entries = merged))
        return AddFactsOutcome.Added(merged.drop(profile.entries.size))
    }

    private fun newEntry(draft: FactDraft, existing: List<ProfileEntry>): ProfileEntry {
        val detail = draft.detail.trim()
        val bullet = if (detail.isEmpty()) null else EvidenceBullet(id = idGenerator.newId(), text = detail)
        return ProfileEntry(
            id = idAllocator.nextId(draft.category, existing),
            category = draft.category,
            title = draft.title.trim(),
            organization = draft.organization.trim(),
            startDate = draft.startDate.trim(),
            endDate = draft.endDate.trim(),
            bullets = listOfNotNull(bullet),
            source = FactSource.USER_STATED,
            isConfirmed = false,
        )
    }

    private fun FactDraft.hasContent(): Boolean =
        title.isNotBlank() ||
            organization.isNotBlank() ||
            startDate.isNotBlank() ||
            endDate.isNotBlank() ||
            detail.isNotBlank()
}
