package com.hirehop.core.testing.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.model.ContentReport
import com.hirehop.core.model.ReportedItemKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

abstract class ContentReportRepositoryContractTest {

    protected abstract fun createContentReportRepository(): ContentReportRepository

    private val at = Instant.fromEpochMilliseconds(1_790_000_000_000)

    private fun report(applicationId: String, kind: ReportedItemKind, itemId: String) =
        ContentReport(applicationId, kind, itemId, at)

    @Test
    fun aReportKeepsEveryField() = runTest {
        val reports = createContentReportRepository()
        val saved = report("app-1", ReportedItemKind.RESUME_BULLET, "b1")

        reports.report(saved)

        assertThat(reports.observeReports("app-1").first()).containsExactly(saved)
    }

    @Test
    fun reportedIdsAreFilteredByKind() = runTest {
        val reports = createContentReportRepository()
        reports.report(report("app-1", ReportedItemKind.RESUME_BULLET, "b1"))
        reports.report(report("app-1", ReportedItemKind.PREP_QUESTION, "q1"))

        assertThat(reports.observeReportedIds("app-1", ReportedItemKind.RESUME_BULLET).first()).containsExactly("b1")
        assertThat(reports.observeReportedIds("app-1", ReportedItemKind.REQUIREMENT).first()).isEmpty()
    }

    @Test
    fun theSameItemReportedTwiceIsKeptOnce() = runTest {
        val reports = createContentReportRepository()

        reports.report(report("app-1", ReportedItemKind.COVER_LETTER, "letter"))
        reports.report(report("app-1", ReportedItemKind.COVER_LETTER, "letter"))

        assertThat(reports.observeReports("app-1").first()).hasSize(1)
    }

    @Test
    fun eachApplicationHasItsOwnReports() = runTest {
        val reports = createContentReportRepository()
        reports.report(report("app-1", ReportedItemKind.RESUME_BULLET, "b1"))

        assertThat(reports.observeReportedIds("app-2", ReportedItemKind.RESUME_BULLET).first()).isEmpty()
    }

    @Test
    fun clearForRemovesOnlyThatApplication() = runTest {
        val reports = createContentReportRepository()
        reports.report(report("app-1", ReportedItemKind.RESUME_BULLET, "b1"))
        reports.report(report("app-2", ReportedItemKind.RESUME_BULLET, "b2"))

        reports.clearFor("app-1")

        assertThat(reports.observeReports("app-1").first()).isEmpty()
        assertThat(reports.observeReportedIds("app-2", ReportedItemKind.RESUME_BULLET).first()).containsExactly("b2")
    }
}
