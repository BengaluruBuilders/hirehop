package com.hirehop.core.domain.account

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.FakeApplicationRepository
import com.hirehop.core.domain.FakeProfileRepository
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.testing.account.TestAccountDataExporter
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.sampleApplication
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.repository.TestExportHistoryRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.TestClock
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CollectAccountDataUseCaseTest {

    private val session = TestSessionRepository()
    private val profiles = FakeProfileRepository(canonicalCandidateProfile)
    private val applications = FakeApplicationRepository()
    private val exports = TestExportHistoryRepository()
    private val payments = TestPaymentGateway()
    private val clock = TestClock()
    private val collect = CollectAccountDataUseCase(session, profiles, applications, exports, payments, clock)

    @Test
    fun theSnapshotHoldsEverythingTheAccountOwns() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        applications.upsertApplication(sampleApplication)
        payments.purchase(ApplicationPack.SINGLE_APPLICATION)

        val data = collect()

        assertThat(data.generatedAt).isEqualTo(clock.instant)
        assertThat(data.account).isEqualTo(SignInAccount.localAccount)
        assertThat(data.profile).isEqualTo(canonicalCandidateProfile)
        assertThat(data.applications).containsExactly(sampleApplication)
        assertThat(data.entitlement.purchasedCredits).isEqualTo(1)
        assertThat(data.purchases).hasSize(1)
    }

    @Test
    fun exportingHandsTheSnapshotToTheExporter() = runTest {
        val exporter = TestAccountDataExporter()

        val archive = ExportAccountDataUseCase(collect, exporter)()

        assertThat(exporter.exported).hasSize(1)
        assertThat(archive.file.isFile).isTrue()
    }
}
