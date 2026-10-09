package com.tailormyresume.feature.settings.impl.yourdata

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.account.AccountData
import com.tailormyresume.core.domain.account.AccountDataArchive
import com.tailormyresume.core.domain.account.AccountDataExporter
import com.tailormyresume.core.domain.account.CollectAccountDataUseCase
import com.tailormyresume.core.domain.account.DeleteMyDataUseCase
import com.tailormyresume.core.domain.account.ExportAccountDataUseCase
import com.tailormyresume.core.domain.account.ExportedFiles
import com.tailormyresume.core.domain.account.TransientDataCleaner
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.core.testing.account.TestAccountDataExporter
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.settings.api.navigation.YourDataNavKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class YourDataViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository().apply {
        sendApplications(listOf(canonicalApplication, canonicalApplication.copy(id = SECOND_ID)))
    }
    private val profileRepository = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
    private val exportHistory = TestExportHistoryRepository()
    private val paymentGateway = TestPaymentGateway()
    private val connectivity = TestConnectivityMonitor()
    private val exporter = TestAccountDataExporter()

    @Test
    fun content_countsTheRealProfileFacts() = runTest {
        profileRepository.sendProfile(
            canonicalCandidateProfile.copy(
                entries = canonicalCandidateProfile.entries.map { entry -> entry.copy(isConfirmed = true) },
            ),
        )
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        val content = viewModel.content()
        assertThat(content.profileFactCount).isEqualTo(27)
        assertThat(content.confirmedFactCount).isEqualTo(24)
        assertThat(content.userStatedFactCount).isEqualTo(3)
    }

    @Test
    fun content_listsEveryApplication() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        assertThat(viewModel.content().applications.map { item -> item.id })
            .containsExactly(canonicalApplication.id, SECOND_ID)
    }

    @Test
    fun content_withoutAPurchase_listsNone() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        assertThat(viewModel.content().purchases).isEmpty()
    }

    @Test
    fun content_afterAPurchase_listsItWithThePackDetails() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        val purchase = viewModel.content().purchases.single()
        assertThat(purchase.credits).isEqualTo(5)
        assertThat(purchase.priceInPaise).isEqualTo(14_900L)
        assertThat(purchase.isPending).isFalse()
    }

    @Test
    fun content_whenTheDeviceGoesOffline_isOffline() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        connectivity.setOnline(false)

        assertThat(viewModel.content().isOffline).isTrue()
    }

    @Test
    fun download_collectsTheDataAndSharesTheArchive() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        viewModel.onDownload()

        val event = viewModel.events.first() as YourDataEvent.ShareArchive
        assertThat(exporter.exported).hasSize(1)
        assertThat(exporter.exported.single().applications).hasSize(2)
        assertThat(event.file.name).startsWith("test-account-data")
        assertThat(viewModel.content().export).isEqualTo(YourDataExport.IDLE)
    }

    @Test
    fun download_whileTheExportRuns_isPreparingAndIgnoresASecondTap() = runTest {
        val gate = CompletableDeferred<Unit>()
        val slowExporter = object : AccountDataExporter {
            var calls = 0

            override suspend fun export(data: AccountData): AccountDataArchive {
                calls += 1
                gate.await()
                return AccountDataArchive(fileName = "x.zip", file = File("x.zip"))
            }
        }
        val viewModel = viewModel(slowExporter)
        collectState(viewModel)

        viewModel.onDownload()
        viewModel.onDownload()

        assertThat(viewModel.content().export).isEqualTo(YourDataExport.PREPARING)
        assertThat(slowExporter.calls).isEqualTo(1)
        gate.complete(Unit)
        assertThat(viewModel.content().export).isEqualTo(YourDataExport.IDLE)
    }

    @Test
    fun download_whenTheExportFails_showsTheFailure() = runTest {
        val failingExporter = object : AccountDataExporter {
            override suspend fun export(data: AccountData): AccountDataArchive =
                throw IllegalStateException("disk full")
        }
        val viewModel = viewModel(failingExporter)
        collectState(viewModel)

        viewModel.onDownload()

        assertThat(viewModel.content().export).isEqualTo(YourDataExport.FAILED)
    }

    @Test
    fun download_whenOffline_doesNothing() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)
        connectivity.setOnline(false)

        viewModel.onDownload()

        assertThat(exporter.exported).isEmpty()
        assertThat(viewModel.content().export).isEqualTo(YourDataExport.IDLE)
    }

    @Test
    fun onEnter_withTheExportingScenario_showsPreparing() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.EXPORTING))

        assertThat(viewModel.content().export).isEqualTo(YourDataExport.PREPARING)
        assertThat(exporter.exported).isEmpty()
    }

    @Test
    fun deleteRequested_namesTheApplication() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        viewModel.onDeleteRequested(SECOND_ID)

        assertThat(viewModel.content().deleteTarget?.id).isEqualTo(SECOND_ID)
    }

    @Test
    fun deleteRequested_forAnUnknownApplication_opensNoDialog() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)

        viewModel.onDeleteRequested("not-an-application")

        assertThat(viewModel.content().deleteTarget).isNull()
    }

    @Test
    fun deleteRequested_whenOffline_opensNoDialog() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)
        connectivity.setOnline(false)

        viewModel.onDeleteRequested(SECOND_ID)

        assertThat(viewModel.content().deleteTarget).isNull()
    }

    @Test
    fun deleteDismissed_keepsTheApplication() = runTest {
        val viewModel = viewModel(exporter)
        collectState(viewModel)
        viewModel.onDeleteRequested(SECOND_ID)

        viewModel.onDeleteDismissed()

        assertThat(viewModel.content().deleteTarget).isNull()
        assertThat(viewModel.content().applications).hasSize(2)
    }

    @Test
    fun deleteConfirmed_removesOnlyThatApplicationAndItsExportHistory() = runTest {
        exportHistory.sendExports(
            listOf(
                exportRecord(applicationId = SECOND_ID),
                exportRecord(applicationId = canonicalApplication.id),
            ),
        )
        val viewModel = viewModel(exporter)
        collectState(viewModel)
        viewModel.onDeleteRequested(SECOND_ID)

        viewModel.onDeleteConfirmed()

        assertThat(viewModel.content().applications.map { item -> item.id })
            .containsExactly(canonicalApplication.id)
        assertThat(viewModel.content().deleteTarget).isNull()
        assertThat(exportHistory.observeExports().first().map { record -> record.applicationId })
            .containsExactly(canonicalApplication.id)
        assertThat(viewModel.content().profileFactCount).isEqualTo(27)
    }

    private fun exportRecord(applicationId: String) = ExportRecord(
        applicationId = applicationId,
        format = ExportFormat.PDF,
        fileName = "resume.pdf",
        exportedAt = TestClock().now(),
        creditKind = CreditKind.FREE,
    )

    private fun viewModel(accountDataExporter: AccountDataExporter): YourDataViewModel {
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

    private companion object {
        const val SECOND_ID = "application-second"
    }
}
