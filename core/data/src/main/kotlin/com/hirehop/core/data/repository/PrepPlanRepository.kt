package com.hirehop.core.data.repository

import com.hirehop.core.model.PrepPlanItem
import kotlinx.coroutines.flow.Flow

interface PrepPlanRepository {
    fun observeItems(applicationId: String): Flow<List<PrepPlanItem>>

    suspend fun add(applicationId: String, item: PrepPlanItem)

    suspend fun remove(applicationId: String, itemId: String)

    suspend fun setDone(applicationId: String, itemId: String, done: Boolean)

    suspend fun clearFor(applicationId: String)
}
