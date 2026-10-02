package com.hirehop.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.CreditKind
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.ExportHistoryRepositoryContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class StoredExportHistoryRepositoryTest : ExportHistoryRepositoryContractTest() {

    override fun createExportHistoryRepository(): ExportHistoryRepository =
        StoredExportHistoryRepository(TestMockStateStore())

    @Test
    fun theHistorySurvivesARestartOfTheRepository() = runTest {
        val store = TestMockStateStore()
        val export = ExportRecord(
            applicationId = "app-1",
            format = ExportFormat.DOCX,
            fileName = "resume.docx",
            exportedAt = Instant.fromEpochMilliseconds(1_790_000_000_000),
            creditKind = CreditKind.PURCHASED,
        )
        StoredExportHistoryRepository(store).record(export)

        val restarted = StoredExportHistoryRepository(store)

        assertThat(restarted.observeExports().first()).containsExactly(export)
    }

    @Test
    fun aStoredExportWithAnUnknownFormatIsSkipped() = runTest {
        val store = TestMockStateStore()
        store.write(
            "exports.history",
            """[{"applicationId":"a","format":"RTF","fileName":"x","exportedAtMillis":1,"creditKind":null}]""",
        )

        assertThat(StoredExportHistoryRepository(store).observeExports().first()).isEmpty()
    }
}
