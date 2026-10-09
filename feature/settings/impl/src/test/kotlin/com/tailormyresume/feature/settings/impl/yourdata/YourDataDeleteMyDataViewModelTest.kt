package com.tailormyresume.feature.settings.impl.yourdata

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
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
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class YourDataDeleteMyDataViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository().apply {
        sendApplications(listOf(canonicalApplication, canonicalApplication.copy(id = "application-second")))
    }
    private val profileRepository = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
    private val exportHistory = TestExportHistoryRepository()
    private val paymentGateway = TestPaymentGateway()
    private val connectivity = TestConnectivityMonitor()
    private val sessionRepository = TestSessionRepository()
    private var fileDeletions = 0
    private var fileDeletionGate: CompletableDeferred<Unit>? = null
    private var fileDeletionFails = false

    private fun viewModel(): YourDataViewModel = YourDataViewModel(
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
            exporter = TestAccountDataExporter(),
        ),
        deleteMyData = DeleteMyDataUseCase(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            exportHistoryRepository = exportHistory,
            sessionRepository = sessionRepository,
            discardJobDrafts = DiscardJobDraftsUseCase(TestPrepPlanRepository(), TestContentReportRepository()),
            exportedFiles = ExportedFiles {
                fileDeletionGate?.await()
                if (fileDeletionFails) throw IllegalStateException("cache locked")
                fileDeletions++
            },
            transientData = TransientDataCleaner { },
        ),
    )

    @Test
    fun request_opensTheConfirmationAndDeletesNothing() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onDeleteMyDataRequested()

        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.CONFIRMING)
        assertThat(viewModel.content().applications).hasSize(2)
        assertThat(viewModel.content().profileFactCount).isEqualTo(27)
        assertThat(fileDeletions).isEqualTo(0)
    }

    @Test
    fun request_whenOffline_opensNoConfirmation() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)
        connectivity.setOnline(false)

        viewModel.onDeleteMyDataRequested()

        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.IDLE)
    }

    @Test
    fun dismiss_closesTheConfirmationAndDeletesNothing() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)
        viewModel.onDeleteMyDataRequested()

        viewModel.onDeleteMyDataDismissed()

        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.IDLE)
        assertThat(viewModel.content().applications).hasSize(2)
        assertThat(profileRepository.observeProfile().first()).isNotNull()
        assertThat(fileDeletions).isEqualTo(0)
    }

    @Test
    fun confirm_withoutARequest_deletesNothing() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onDeleteMyDataConfirmed()

        assertThat(viewModel.content().applications).hasSize(2)
        assertThat(profileRepository.observeProfile().first()).isNotNull()
    }

    @Test
    fun confirm_whenOffline_deletesNothing() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)
        viewModel.onDeleteMyDataRequested()
        connectivity.setOnline(false)

        viewModel.onDeleteMyDataConfirmed()

        assertThat(viewModel.content().applications).hasSize(2)
        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.IDLE)
    }

    @Test
    fun confirm_deletesTheDataAndKeepsThePurchases() = runTest {
        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        val entitlementBefore = paymentGateway.entitlement()
        val viewModel = viewModel()
        collectState(viewModel)
        viewModel.onDeleteMyDataRequested()

        viewModel.onDeleteMyDataConfirmed()

        val content = viewModel.content()
        assertThat(content.profileFactCount).isEqualTo(0)
        assertThat(content.applications).isEmpty()
        assertThat(content.deletion).isEqualTo(YourDataDeletion.IDLE)
        assertThat(content.purchases).hasSize(1)
        assertThat(paymentGateway.entitlement()).isEqualTo(entitlementBefore)
        assertThat(fileDeletions).isEqualTo(1)
    }

    @Test
    fun confirm_twice_startsOneDeletion() = runTest {
        fileDeletionGate = CompletableDeferred()
        val viewModel = viewModel()
        collectState(viewModel)
        viewModel.onDeleteMyDataRequested()

        viewModel.onDeleteMyDataConfirmed()
        viewModel.onDeleteMyDataConfirmed()
        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.DELETING)
        fileDeletionGate?.complete(Unit)

        assertThat(fileDeletions).isEqualTo(1)
        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.IDLE)
    }

    @Test
    fun whileDeleting_downloadAndANewRequestAreIgnored() = runTest {
        fileDeletionGate = CompletableDeferred()
        val viewModel = viewModel()
        collectState(viewModel)
        viewModel.onDeleteMyDataRequested()
        viewModel.onDeleteMyDataConfirmed()

        viewModel.onDownload()
        viewModel.onDeleteMyDataRequested()

        assertThat(viewModel.content().export).isEqualTo(YourDataExport.IDLE)
        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.DELETING)
        fileDeletionGate?.complete(Unit)
    }

    @Test
    fun whenDeletionFails_showsTheFailureAndARetryFinishesIt() = runTest {
        fileDeletionFails = true
        val viewModel = viewModel()
        collectState(viewModel)
        viewModel.onDeleteMyDataRequested()

        viewModel.onDeleteMyDataConfirmed()

        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.FAILED)

        fileDeletionFails = false
        viewModel.onDeleteMyDataRequested()
        viewModel.onDeleteMyDataConfirmed()

        assertThat(viewModel.content().deletion).isEqualTo(YourDataDeletion.IDLE)
        assertThat(viewModel.content().applications).isEmpty()
        assertThat(fileDeletions).isEqualTo(1)
    }

    private fun TestScope.collectState(viewModel: YourDataViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun YourDataViewModel.content(): YourDataUiState.Content =
        uiState.value as YourDataUiState.Content
}
