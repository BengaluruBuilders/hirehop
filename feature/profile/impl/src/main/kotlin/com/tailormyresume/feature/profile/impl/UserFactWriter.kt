package com.tailormyresume.feature.profile.impl

import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.fact.AddFactsOutcome
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftError
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ProfileEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal data class ContactInput(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
) {
    val isBlank: Boolean get() = fullName.isBlank() && email.isBlank() && phone.isBlank()
}

internal sealed interface FactWriteResult {
    data class Written(val entries: List<ProfileEntry>) : FactWriteResult

    data class Rejected(val errors: List<FactDraftError>) : FactWriteResult

    data object NothingToWrite : FactWriteResult
}

internal class UserFactWriter @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val addUserStatedFacts: AddUserStatedFactsUseCase,
) {

    fun observeEntries(): Flow<List<ProfileEntry>> = profileRepository.observeProfile().map { it?.entries.orEmpty() }

    suspend fun write(
        drafts: List<FactDraft>,
        replacing: Set<String> = emptySet(),
        contact: ContactInput = ContactInput(),
        skills: List<String> = emptyList(),
    ): FactWriteResult {
        val changesProfile = replacing.isNotEmpty() || !contact.isBlank || skills.isNotEmpty()
        if (changesProfile) updateProfile(replacing, contact, skills)
        return when (val outcome = addUserStatedFacts(drafts)) {
            is AddFactsOutcome.Added -> FactWriteResult.Written(outcome.entries)
            is AddFactsOutcome.Rejected -> FactWriteResult.Rejected(outcome.errors)
            AddFactsOutcome.NothingToAdd ->
                if (changesProfile) FactWriteResult.Written(emptyList()) else FactWriteResult.NothingToWrite
        }
    }

    private suspend fun updateProfile(replacing: Set<String>, contact: ContactInput, skills: List<String>) {
        val profile = profileRepository.observeProfile().first() ?: blankProfile()
        val added = newSkills(profile.skills, skills)
        profileRepository.saveProfile(
            profile.withContact(contact).copy(
                skills = profile.skills + added,
                userStatedSkills = profile.userStatedSkills + added,
                entries = profile.entries.filterNot { it.id in replacing },
            ),
        )
    }

    private fun CandidateProfile.withContact(contact: ContactInput): CandidateProfile = copy(
        fullName = contact.fullName.trim().ifEmpty { fullName },
        email = contact.email.trim().ifEmpty { email },
        phone = contact.phone.trim().ifEmpty { phone },
    )

    private fun newSkills(existing: List<String>, typed: List<String>): List<String> {
        val known = existing.map { it.lowercase() }.toMutableSet()
        return typed.map { it.trim() }.filter { it.isNotEmpty() && known.add(it.lowercase()) }
    }

    private fun blankProfile() = CandidateProfile(
        fullName = "",
        email = "",
        phone = "",
        headline = "",
        skills = emptyList(),
        entries = emptyList(),
    )
}
