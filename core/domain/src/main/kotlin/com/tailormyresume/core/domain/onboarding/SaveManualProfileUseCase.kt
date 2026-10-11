package com.tailormyresume.core.domain.onboarding

import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import javax.inject.Inject

data class ManualContact(
    val fullName: String,
    val email: String,
    val phone: String,
    val city: String,
    val jobTitle: String,
    val company: String,
)

class SaveManualProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val factIdAllocator: FactIdAllocator,
) {
    suspend operator fun invoke(contact: ManualContact): CandidateProfile = throw NotImplementedError()
}
