package com.tailormyresume.feature.settings.impl.yourdata

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.account.AccountDataExporter
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class YourDataFirstContentTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository().apply {
        sendApplications(listOf(canonicalApplication, canonicalApplication.copy(id = SECOND_ID)))
    }
    private val profileRepository = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
    private val exportHistory = TestExportHistoryRepository()
    private val connectivity = TestConnectivityMonitor()
    private val exporter = TestAccountDataExporter()

    @Test
    fun packsNeverReturning_stillEmitsContentWithProfileApplicationsAndPurchases() = runTest {
        val gateway = GatedPaymentGateway()
        val viewModel = viewModel(gateway, exporter)
        collectState(viewModel)

        val content = viewModel.content()
        assertThat(content.applications).hasSize(2)
        assertThat(content.profileFactCount).isGreaterThan(0)

        gateway.delegate.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(viewModel.content().purchases).hasSize(1)
        assertThat(viewModel.content().purchases.single().credits).isNull()
    }

    @Test
    fun packsArriving_updatesThePurchaseRows() = runTest {
        val gateway = GatedPaymentGateway()
        val viewModel = viewModel(gateway, exporter)
        collectState(viewModel)
        gateway.delegate.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        gateway.packsDeferred.complete(gateway.delegate.packs())
        testScheduler.advanceUntilIdle()

        assertThat(viewModel.content().purchases).hasSize(1)
        assertThat(viewModel.content().purchases.single().credits).isEqualTo(5)
    }

    private fun viewModel(
        paymentGateway: PaymentGateway,
        accountDataExporter: AccountDataExporter,
    ): YourDataViewModel {
        val sessionRepository = TestSessionRepository()
        return YourDataViewModel(
            profileRepository = profileRepository,
            paymentGateway = paymentGateway,
            connectivityMonitor = connectivity,
            applicationRepository = applicationRepository,
            exportHistoryRepository = exportHistory,
            exportAccountData = ExportAccountDataUseCase(
                collectAccountData = CollectAccountDataUseCase(
                    sessionRepository = sessionRepository,
                    profileRepository = profileRepository,
                    applicationRepository = applicationRepository,
                    exportHistoryRepository = exportHistory,
                    coverLetterRepository = TestCoverLetterRepository(),
                    prepPlanRepository = TestPrepPlanRepository(),
                    paymentGateway = paymentGateway,
                    clock = TestClock(),
                ),
                exporter = accountDataExporter,
            ),
            deleteMyData = DeleteMyDataUseCase(
                applicationRepository = applicationRepository,
                profileRepository = profileRepository,
                exportHistoryRepository = exportHistory,
                sessionRepository = sessionRepository,
                discardJobDrafts = DiscardJobDraftsUseCase(TestPrepPlanRepository(), TestContentReportRepository()),
                exportedFiles = ExportedFiles.None,
                transientData = TransientDataCleaner { },
            ),
        )
    }

    private fun TestScope.collectState(viewModel: YourDataViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun YourDataViewModel.content(): YourDataUiState.Content =
        uiState.value as YourDataUiState.Content

    private class GatedPaymentGateway(
        val delegate: TestPaymentGateway = TestPaymentGateway(),
    ) : PaymentGateway by delegate {
        val packsDeferred = CompletableDeferred<List<ApplicationPack>>()

        override suspend fun packs(): List<ApplicationPack> = packsDeferred.await()
    }

    private companion object {
        const val SECOND_ID = "application-second"
    }
}
