package com.hirehop.core.data.repository

import com.hirehop.core.model.ExportRecord
import kotlinx.coroutines.flow.Flow

interface ExportHistoryRepository {
    fun observeExports(): Flow<List<ExportRecord>>

    fun observeExports(applicationId: String): Flow<List<ExportRecord>>

    suspend fun record(export: ExportRecord)

    suspend fun clearFor(applicationId: String)

    suspend fun clear()
}
