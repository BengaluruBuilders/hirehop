package com.hirehop.core.data.repository

import com.hirehop.core.model.TailoringReviewState
import kotlinx.coroutines.flow.Flow

interface TailoringReviewStateRepository {
    fun observe(applicationId: String): Flow<TailoringReviewState>

    suspend fun recordRegeneration(applicationId: String, section: String)

    suspend fun markEdited(applicationId: String, bulletId: String)

    suspend fun clearEdited(applicationId: String, bulletIds: Collection<String>)

    suspend fun clearFor(applicationId: String)
}
