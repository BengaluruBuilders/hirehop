package com.tailormyresume.core.data.repository

import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.ReportedItemKind
import kotlinx.coroutines.flow.Flow

interface ContentReportRepository {
    suspend fun report(report: ContentReport)

    fun observeReports(applicationId: String): Flow<List<ContentReport>>

    fun observeReportedIds(applicationId: String, kind: ReportedItemKind): Flow<Set<String>>

    suspend fun clearFor(applicationId: String)
}
