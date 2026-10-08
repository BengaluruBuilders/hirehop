package com.tailormyresume.core.data.repository

import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.data.mock.readValue
import com.tailormyresume.core.data.mock.writeValue
import com.tailormyresume.core.model.ContentReport
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingReportQueue @Inject constructor(private val store: MockStateStore) {

    private val mutex = Mutex()
    private val serializer = ListSerializer(ReportDto.serializer())

    suspend fun add(report: ContentReport) = update { current ->
        if (current.any { it.sameItemAs(report) }) current else current + report.toDto()
    }

    suspend fun remove(report: ContentReport) = update { current -> current.filterNot { it.sameItemAs(report) } }

    suspend fun pending(): List<ContentReport> =
        mutex.withLock { store.readValue(KEY, serializer).orEmpty() }.mapNotNull(ReportDto::toModel)

    suspend fun clear() = mutex.withLock { store.remove(KEY) }

    suspend fun clearFor(applicationId: String) = update { current -> current.filterNot { it.applicationId == applicationId } }

    private suspend fun update(change: (List<ReportDto>) -> List<ReportDto>) {
        mutex.withLock { store.writeValue(KEY, serializer, change(store.readValue(KEY, serializer).orEmpty())) }
    }

    private fun ReportDto.sameItemAs(report: ContentReport) =
        applicationId == report.applicationId && itemKind == report.itemKind.name && itemId == report.itemId

    private companion object {
        const val KEY = "reports.content.pending"
    }
}
