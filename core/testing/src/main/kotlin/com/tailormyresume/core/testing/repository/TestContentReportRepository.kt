package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.ReportedItemKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestContentReportRepository : ContentReportRepository {

    private val reports = MutableStateFlow<List<ContentReport>>(emptyList())

    override suspend fun report(report: ContentReport) {
        reports.update { list ->
            val known = list.any {
                it.applicationId == report.applicationId && it.itemKind == report.itemKind && it.itemId == report.itemId
            }
            if (known) list else list + report
        }
    }

    override fun observeReports(applicationId: String): Flow<List<ContentReport>> =
        reports.map { list -> list.filter { it.applicationId == applicationId } }

    override fun observeReportedIds(applicationId: String, kind: ReportedItemKind): Flow<Set<String>> =
        observeReports(applicationId).map { list -> list.filter { it.itemKind == kind }.map { it.itemId }.toSet() }

    override suspend fun clearFor(applicationId: String) {
        reports.update { list -> list.filterNot { it.applicationId == applicationId } }
    }
}
