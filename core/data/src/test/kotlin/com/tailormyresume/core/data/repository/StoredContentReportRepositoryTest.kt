package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.ContentReportRepositoryContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class StoredContentReportRepositoryTest : ContentReportRepositoryContractTest() {

    override fun createContentReportRepository(): ContentReportRepository =
        StoredContentReportRepository(TestMockStateStore())

    @Test
    fun reportsSurviveARestartOfTheRepository() = runTest {
        val store = TestMockStateStore()
        val report = ContentReport("app-1", ReportedItemKind.PREP_QUESTION, "q1", "text", Instant.fromEpochMilliseconds(5))
        StoredContentReportRepository(store).report(report)

        assertThat(StoredContentReportRepository(store).observeReports("app-1").first()).containsExactly(report)
    }
}
