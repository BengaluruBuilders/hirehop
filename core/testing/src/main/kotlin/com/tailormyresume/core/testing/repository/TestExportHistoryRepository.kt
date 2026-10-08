package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.model.ExportRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestExportHistoryRepository : ExportHistoryRepository {

    private val records = MutableStateFlow<List<ExportRecord>>(emptyList())

    override fun observeExports(): Flow<List<ExportRecord>> = records

    override fun observeExports(applicationId: String): Flow<List<ExportRecord>> =
        records.map { list -> list.filter { it.applicationId == applicationId } }

    override suspend fun record(export: ExportRecord) {
        records.update { it + export }
    }

    override suspend fun clearFor(applicationId: String) {
        records.update { list -> list.filterNot { it.applicationId == applicationId } }
    }

    override suspend fun clear() {
        records.value = emptyList()
    }

    fun sendExports(exports: List<ExportRecord>) {
        records.value = exports
    }
}
