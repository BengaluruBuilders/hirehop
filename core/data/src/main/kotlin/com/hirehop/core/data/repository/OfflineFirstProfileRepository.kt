package com.hirehop.core.data.repository

import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers.IO
import com.hirehop.core.data.model.asEntity
import com.hirehop.core.data.model.asEntryEntities
import com.hirehop.core.data.model.asExternalModel
import com.hirehop.core.database.dao.ProfileDao
import com.hirehop.core.model.CandidateProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineFirstProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override fun observeProfile(): Flow<CandidateProfile?> =
        profileDao.observePopulatedProfile()
            .map { populated -> populated?.asExternalModel() }
            .flowOn(ioDispatcher)

    override suspend fun saveProfile(profile: CandidateProfile) {
        val duplicateIds = profile.entries
            .groupingBy { it.id }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys
        require(duplicateIds.isEmpty()) {
            "Profile entries must have unique ids. Duplicate ids: $duplicateIds"
        }
        profileDao.replaceProfile(
            profile = profile.asEntity(),
            entries = profile.asEntryEntities(),
        )
    }

    override suspend fun clearProfile() = profileDao.deleteProfile()
}
