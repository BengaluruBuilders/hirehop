package com.tailormyresume.feature.settings.impl.yourdata

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseRecord
import com.tailormyresume.core.domain.account.CollectAccountDataUseCase
import com.tailormyresume.core.domain.account.DeleteMyDataUseCase
import com.tailormyresume.core.domain.account.ExportAccountDataUseCase
import com.tailormyresume.core.domain.account.ExportedFiles
import com.tailormyresume.core.domain.account.TransientDataCleaner
import com.tailormyresume.core.testing.account.TestAccountDataExporter
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestCoverLetterRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private class PurchasesFailGateway(delegate: PaymentGateway) : PaymentGateway by delegate {
    override fun observePurchaseHistory(): Flow<List<PurchaseRecord>> = flow { throw IOException("offline") }
}

@OptIn(ExperimentalCoroutinesApi::class)
class YourDataPurchaseFailureTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applications = TestApplicationRepository().apply { sendApplications(listOf(canonicalApplication)) }
    private val profiles = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
    private val exportHistory = TestExportHistoryRepository()
    private val gateway = PurchasesFailGateway(TestPaymentGateway())

    private fun viewModel(): YourDataViewModel {
        val session = TestSessionRepository()
        return YourDataViewModel(
            profileRepository = profiles,
            paymentGateway = gateway,
            connectivityMonitor = TestConnectivityMonitor(),
            applicationRepository = applications,
            exportHistoryRepository = exportHistory,
            exportAccountData = ExportAccountDataUseCase(
                collectAccountData = CollectAccountDataUseCase(
                    sessionRepository = session,
                    profileRepository = profiles,
                    applicationRepository = applications,
                    exportHistoryRepository = exportHistory,
                    coverLetterRepository = TestCoverLetterRepository(),
                    prepPlanRepository = TestPrepPlanRepository(),
                    paymentGateway = gateway,
                    clock = TestClock(),
                ),
                exporter = TestAccountDataExporter(),
            ),
            deleteMyData = DeleteMyDataUseCase(
                applicationRepository = applications,
                profileRepository = profiles,
                exportHistoryRepository = exportHistory,
                sessionRepository = session,
                discardJobDrafts = DiscardJobDraftsUseCase(TestPrepPlanRepository(), TestContentReportRepository()),
                exportedFiles = ExportedFiles.None,
                transientData = TransientDataCleaner { },
            ),
        )
    }

    @Test
    fun aFailedPurchaseHistoryStillShowsTheScreenWithNoPurchases() = runTest {
        val subject = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { subject.uiState.collect {} }

        val content = subject.uiState.value as YourDataUiState.Content
        assertThat(content.purchases).isEmpty()
        assertThat(content.applications).hasSize(1)
    }
}
