package com.tailormyresume.core.domain.onboarding

import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import javax.inject.Inject

class SaveImportedProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val factIdAllocator: FactIdAllocator,
) {
    suspend operator fun invoke(parsed: CandidateProfile, sourceFileName: String?): CandidateProfile =
        throw NotImplementedError()
}
