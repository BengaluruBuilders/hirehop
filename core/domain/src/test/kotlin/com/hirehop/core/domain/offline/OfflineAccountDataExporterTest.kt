package com.hirehop.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.mock.NoMockLatency
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseRecord
import com.hirehop.core.domain.PurchaseState
import com.hirehop.core.domain.account.AccountData
import com.hirehop.core.domain.account.AccountDataExporter
import com.hirehop.core.model.CreditKind
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.testing.account.AccountDataExporterContractTest
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.sampleApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.ZipFile
import kotlin.time.Instant

class OfflineAccountDataExporterTest : AccountDataExporterContractTest() {

    @get:Rule
    val folder = TemporaryFolder()

    override fun createExporter(): AccountDataExporter =
        OfflineAccountDataExporter({ folder.root }, NoMockLatency, Dispatchers.Unconfined)

    @Test
    fun theArchiveHoldsTheFourDataFiles() = runTest {
        val now = Instant.fromEpochMilliseconds(1_790_000_000_000)
        val data = AccountData(
            generatedAt = now,
            account = SignInAccount.localAccount,
            consent = null,
            profile = canonicalCandidateProfile,
            applications = listOf(sampleApplication),
            entitlement = PurchaseEntitlement(0, 4, emptyList()),
            purchases = listOf(PurchaseRecord("application_pack_5", "order-1", now, PurchaseState.COMPLETED)),
            exports = listOf(ExportRecord(sampleApplication.id, ExportFormat.PDF, "a.pdf", now, CreditKind.FREE)),
        )

        val archive = createExporter().export(data)

        ZipFile(archive.file).use { zip ->
            assertThat(zip.entries().toList().map { it.name })
                .containsExactly("account.txt", "profile.txt", "applications.txt", "purchases.txt")
            val purchases = zip.getInputStream(zip.getEntry("purchases.txt")).bufferedReader().readText()
            assertThat(purchases).contains("order-1")
            val applications = zip.getInputStream(zip.getEntry("applications.txt")).bufferedReader().readText()
            assertThat(applications).contains("a.pdf")
        }
    }
}
