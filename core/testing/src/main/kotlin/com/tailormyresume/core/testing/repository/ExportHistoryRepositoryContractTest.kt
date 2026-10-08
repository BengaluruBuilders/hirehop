package com.tailormyresume.core.testing.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

abstract class ExportHistoryRepositoryContractTest {

    protected abstract fun createExportHistoryRepository(): ExportHistoryRepository

    private fun record(applicationId: String, format: ExportFormat = ExportFormat.PDF, kind: CreditKind? = CreditKind.FREE) =
        ExportRecord(
            applicationId = applicationId,
            format = format,
            fileName = "$applicationId.${format.name.lowercase()}",
            exportedAt = Instant.fromEpochMilliseconds(1_790_000_000_000),
            creditKind = kind,
        )

    @Test
    fun aNewHistoryIsEmpty() = runTest {
        val history = createExportHistoryRepository()

        assertThat(history.observeExports().first()).isEmpty()
    }

    @Test
    fun aRecordedExportKeepsEveryField() = runTest {
        val history = createExportHistoryRepository()
        val export = record("app-1", ExportFormat.DOCX, CreditKind.PURCHASED)

        history.record(export)

        assertThat(history.observeExports().first()).containsExactly(export)
    }

    @Test
    fun aRecordedExportKeepsItsPageCountAndTemplateName() = runTest {
        val history = createExportHistoryRepository()
        val export = record("app-1").copy(pageCount = 1, templateName = "Plain")

        history.record(export)

        val saved = history.observeExports().first().single()
        assertThat(saved.pageCount).isEqualTo(1)
        assertThat(saved.templateName).isEqualTo("Plain")
    }

    @Test
    fun anExportWithNoDetailKeepsNullDetail() = runTest {
        val history = createExportHistoryRepository()

        history.record(record("app-1"))

        val saved = history.observeExports().first().single()
        assertThat(saved.pageCount).isNull()
        assertThat(saved.templateName).isNull()
    }

    @Test
    fun anExportWithNoCreditKeepsNoCreditKind() = runTest {
        val history = createExportHistoryRepository()
        val export = record("app-1", kind = null)

        history.record(export)

        assertThat(history.observeExports().first().single().creditKind).isNull()
    }

    @Test
    fun exportsAreKeptInTheOrderTheyWereRecorded() = runTest {
        val history = createExportHistoryRepository()
        val first = record("app-1")
        val second = record("app-1", ExportFormat.DOCX)

        history.record(first)
        history.record(second)

        assertThat(history.observeExports().first()).containsExactly(first, second).inOrder()
    }

    @Test
    fun exportsOfOneApplicationCanBeObservedAlone() = runTest {
        val history = createExportHistoryRepository()
        history.record(record("app-1"))
        history.record(record("app-2"))

        history.observeExports("app-2").test {
            assertThat(awaitItem().map { it.applicationId }).containsExactly("app-2")
            history.record(record("app-2", ExportFormat.DOCX))
            assertThat(awaitItem()).hasSize(2)
        }
    }

    @Test
    fun clearForRemovesOnlyThatApplication() = runTest {
        val history = createExportHistoryRepository()
        history.record(record("app-1"))
        history.record(record("app-2"))

        history.clearFor("app-1")

        assertThat(history.observeExports().first().map { it.applicationId }).containsExactly("app-2")
    }

    @Test
    fun clearRemovesEverything() = runTest {
        val history = createExportHistoryRepository()
        history.record(record("app-1"))
        history.record(record("app-2"))

        history.clear()

        assertThat(history.observeExports().first()).isEmpty()
    }
}
