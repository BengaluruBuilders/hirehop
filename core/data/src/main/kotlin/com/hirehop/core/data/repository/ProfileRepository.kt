package com.hirehop.core.data.repository

import com.hirehop.core.model.CandidateProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<CandidateProfile?>

    suspend fun saveProfile(profile: CandidateProfile)

    suspend fun clearProfile()
}
