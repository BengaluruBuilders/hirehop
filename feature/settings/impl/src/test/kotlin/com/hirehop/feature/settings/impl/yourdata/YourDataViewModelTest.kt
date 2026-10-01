package com.hirehop.feature.settings.impl.yourdata

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.settings.api.navigation.YourDataNavKey
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class YourDataViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var applicationRepository: TestApplicationRepository
    private lateinit var profileRepository: TestProfileRepository
    private lateinit var paymentGateway: TestSettingsPaymentGateway
    private lateinit var viewModel: YourDataViewModel

    @Before
    fun setup() {
        applicationRepository = TestApplicationRepository()
        profileRepository = TestProfileRepository()
        paymentGateway = TestSettingsPaymentGateway(
            entitlement = PurchaseEntitlement(
                freeCredits = 1,
                purchasedCredits = 0,
                pendingPackIds = emptyList(),
            ),
        )
        viewModel = YourDataViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            paymentGateway = paymentGateway,
        )
    }

    @Test
    fun onEnter_beforeAnyEntry_isIdleAndNotOffline() {
        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(YourDataStage.IDLE)
        assertThat(state.isOffline).isFalse()
        assertThat(state.isExporting).isFalse()
    }

    @Test
    fun onEnter_default_countsTheRealProfileFacts() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        val row = viewModel.uiState.value.rowOf(YourDataLedgerKind.PROFILE)
        assertThat(row.count).isEqualTo(18)
        assertThat(row.confirmedFactCount).isEqualTo(15)
        assertThat(row.userStatedFactCount).isEqualTo(3)
        assertThat(row.unit).isEqualTo(YourDataLedgerUnit.FACTS)
        assertThat(viewModel.uiState.value.profileFactCount).isEqualTo(18)
    }

    @Test
    fun onEnter_default_ledgerCarriesTheFourDataKinds() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.ledger.map { row -> row.kind }).containsExactly(
            YourDataLedgerKind.PROFILE,
            YourDataLedgerKind.APPLICATIONS,
            YourDataLedgerKind.PURCHASES,
            YourDataLedgerKind.UPLOADED_RESUME,
        ).inOrder()
    }

    @Test
    fun onEnter_default_applicationsRowListsEveryApplication() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        val row = viewModel.uiState.value.rowOf(YourDataLedgerKind.APPLICATIONS)
        assertThat(row.count).isEqualTo(1)
        assertThat(row.items).hasSize(1)
        assertThat(row.items.single().title).isEqualTo(canonicalApplication.job.title)
        assertThat(row.items.single().company).isEqualTo(canonicalApplication.job.company)
    }

    @Test
    fun onEnter_default_purchasesRowCountsTheRecordedPacksOnThisDevice() = runTest {
        paymentGateway = TestSettingsPaymentGateway(
            entitlement = PurchaseEntitlement(
                freeCredits = 0,
                purchasedCredits = 5,
                pendingPackIds = listOf("application_pack_5"),
            ),
        )
        viewModel = YourDataViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            paymentGateway = paymentGateway,
        )
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        val row = viewModel.uiState.value.rowOf(YourDataLedgerKind.PURCHASES)
        assertThat(row.count).isEqualTo(1)
        assertThat(row.purchaseCount).isEqualTo(1)
        assertThat(row.purchasedCreditCount).isEqualTo(5)
    }

    @Test
    fun onEnter_default_purchasesRowIsZeroWhenNoPackIsRecorded() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.rowOf(YourDataLedgerKind.PURCHASES).count).isEqualTo(0)
    }

    @Test
    fun onEnter_default_uploadedResumeRowHoldsNoFileBecauseItIsDeletedAfterReading() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        val row = viewModel.uiState.value.rowOf(YourDataLedgerKind.UPLOADED_RESUME)
        assertThat(row.count).isEqualTo(0)
        assertThat(row.unit).isEqualTo(YourDataLedgerUnit.FILES)
    }

    @Test
    fun onAction_downloadTapped_namesTheStepsAndStartsAtTheFirstOne() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DownloadTapped)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(YourDataStage.PREPARING)
        assertThat(state.isExporting).isTrue()
        assertThat(state.steps.map { step -> step.kind }).containsExactly(
            YourDataExportStepKind.PROFILE_FACTS,
            YourDataExportStepKind.APPLICATIONS,
            YourDataExportStepKind.PACKING,
        ).inOrder()
        assertThat(state.steps.single { step -> step.isCurrent }.kind)
            .isEqualTo(YourDataExportStepKind.PROFILE_FACTS)
        assertThat(state.steps.none { step -> step.isDone }).isTrue()
    }

    @Test
    fun onAction_downloadTapped_carriesTheRealApplicationCountIntoTheStep() = runTest {
        applicationRepository.sendApplications(List(4) { canonicalApplication.copy(id = "app-$it") })
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DownloadTapped)

        val applicationsStep = viewModel.uiState.value.steps
            .single { step -> step.kind == YourDataExportStepKind.APPLICATIONS }
        assertThat(applicationsStep.applicationCount).isEqualTo(4)
    }

    @Test
    fun onAction_downloadTapped_neverShowsAPercentage() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DownloadTapped)
        advanceTimeBy(EXPORT_STEP_DELAY_MS + 1)

        val state = viewModel.uiState.value
        assertThat(state.isExporting).isTrue()
        assertThat(state.steps.single { step -> step.isCurrent }.kind)
            .isEqualTo(YourDataExportStepKind.APPLICATIONS)
        assertThat(state.steps.single { step -> step.isDone }.kind)
            .isEqualTo(YourDataExportStepKind.PROFILE_FACTS)
    }

    @Test
    fun onAction_downloadTapped_whenFinished_reportsTheReadyFileName() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DownloadTapped)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(YourDataStage.READY)
        assertThat(state.isExporting).isFalse()
        assertThat(state.exportFileName).isEqualTo("HireHop-data_Priya-Deshmukh.zip")
    }

    @Test
    fun onAction_downloadTapped_twice_doesNotStartASecondExport() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DownloadTapped)
        val firstSteps = viewModel.uiState.value.steps
        viewModel.onAction(YourDataAction.DownloadTapped)

        assertThat(viewModel.uiState.value.steps).isEqualTo(firstSteps)
    }

    @Test
    fun onAction_offline_ignoresTheDownload() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.OFFLINE))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DownloadTapped)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.stage).isEqualTo(YourDataStage.IDLE)
        assertThat(state.exportFileName).isNull()
    }

    @Test
    fun onAction_offline_keepsTheLedgerReadable() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.OFFLINE))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.rowOf(YourDataLedgerKind.PROFILE).count).isEqualTo(18)
        assertThat(viewModel.uiState.value.rowOf(YourDataLedgerKind.APPLICATIONS).count).isEqualTo(1)
    }

    @Test
    fun onAction_deleteRequested_onAnApplicationRow_namesItExactly() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DeleteRequested(applicationId = canonicalApplication.id))

        val target = viewModel.uiState.value.deleteTarget
        assertThat(target).isNotNull()
        assertThat(target?.title).isEqualTo(canonicalApplication.job.title)
        assertThat(target?.company).isEqualTo(canonicalApplication.job.company)
        assertThat(viewModel.uiState.value.canDeleteApplications).isTrue()
    }

    @Test
    fun onAction_deleteRequested_forAnUnknownApplication_keepsTheDialogClosed() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DeleteRequested(applicationId = "not-a-real-id"))

        assertThat(viewModel.uiState.value.deleteTarget).isNull()
    }

    @Test
    fun onAction_deleteConfirmed_removesOnlyThatApplication() = runTest {
        applicationRepository.sendApplications(
            listOf(
                canonicalApplication.copy(id = "keep-me"),
                canonicalApplication.copy(id = "delete-me"),
            ),
        )
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DeleteRequested(applicationId = "delete-me"))
        viewModel.onAction(YourDataAction.DeleteConfirmed)
        advanceUntilIdle()

        val row = viewModel.uiState.value.rowOf(YourDataLedgerKind.APPLICATIONS)
        assertThat(row.count).isEqualTo(1)
        assertThat(row.items.single().applicationId).isEqualTo("keep-me")
        assertThat(viewModel.uiState.value.deleteTarget).isNull()
    }

    @Test
    fun onAction_deleteConfirmed_keepsTheProfileFacts() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DeleteRequested(applicationId = canonicalApplication.id))
        viewModel.onAction(YourDataAction.DeleteConfirmed)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.rowOf(YourDataLedgerKind.PROFILE).count).isEqualTo(18)
        assertThat(viewModel.uiState.value.profileFactCount).isEqualTo(18)
    }

    @Test
    fun onAction_deleteDismissed_keepsTheApplication() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.DeleteRequested(applicationId = canonicalApplication.id))
        viewModel.onAction(YourDataAction.DeleteDismissed)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deleteTarget).isNull()
        assertThat(viewModel.uiState.value.rowOf(YourDataLedgerKind.APPLICATIONS).count).isEqualTo(1)
    }

    @Test
    fun onAction_correctOnTheProfileRow_asksForTheCorrectionScreen() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(
            YourDataAction.LedgerActionTapped(
                kind = YourDataLedgerKind.PROFILE,
                action = YourDataLedgerAction.CORRECT,
            ),
        )

        assertThat(viewModel.uiState.value.destination).isEqualTo(YourDataDestination.PROFILE_CORRECT)
    }

    @Test
    fun onAction_viewOnTheProfileRow_asksForTheProfileScreen() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(
            YourDataAction.LedgerActionTapped(
                kind = YourDataLedgerKind.PROFILE,
                action = YourDataLedgerAction.VIEW,
            ),
        )

        assertThat(viewModel.uiState.value.destination).isEqualTo(YourDataDestination.PROFILE_VIEW)
    }

    @Test
    fun onAction_onTheUploadedResumeRow_hasNowhereToGoAndStaysPut() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(
            YourDataAction.LedgerActionTapped(
                kind = YourDataLedgerKind.UPLOADED_RESUME,
                action = YourDataLedgerAction.VIEW,
            ),
        )

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun onAction_share_whenNotReady_asksForNothing() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.ShareTapped)

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun onAction_share_whenReady_asksForTheShareSheet() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()
        viewModel.onAction(YourDataAction.DownloadTapped)
        advanceUntilIdle()

        viewModel.onAction(YourDataAction.ShareTapped)

        assertThat(viewModel.uiState.value.destination).isEqualTo(YourDataDestination.SHARE_SHEET)
    }

    @Test
    fun onAction_destinationConsumed_clearsTheDestination() = runTest {
        seedCanonicalData()
        viewModel.onEnter(YourDataNavKey(scenario = DebugScenario.DEFAULT))
        advanceUntilIdle()
        viewModel.onAction(YourDataAction.ShareTapped)
        viewModel.onAction(YourDataAction.DestinationSelected(YourDataDestination.PROFILE_VIEW))

        viewModel.onAction(YourDataAction.DestinationConsumed)

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun exportFileName_forAnEmptyProfile_doesNotInventAName() {
        assertThat(yourDataExportFileName(fullName = null)).isEqualTo("HireHop-data.zip")
        assertThat(yourDataExportFileName(fullName = "   ")).isEqualTo("HireHop-data.zip")
    }

    @Test
    fun exportFileName_joinsTheRealNameParts() {
        assertThat(yourDataExportFileName(fullName = "Priya Deshmukh"))
            .isEqualTo("HireHop-data_Priya-Deshmukh.zip")
    }

    @Test
    fun exportSteps_neverCarryAPercentage() {
        val steps = yourDataExportSteps(currentIndex = 1, applicationCount = 4)

        assertThat(steps).hasSize(3)
        assertThat(steps.count { step -> step.isDone }).isEqualTo(1)
        assertThat(steps.count { step -> step.isCurrent }).isEqualTo(1)
        assertThat(steps.last().isDone).isFalse()
    }

    @Test
    fun ledger_whenTheProfileIsAbsent_doesNotInventFacts() {
        val ledger = yourDataLedger(
            profile = null,
            applications = emptyList(),
            entitlement = null,
        )

        assertThat(ledger.first { row -> row.kind == YourDataLedgerKind.PROFILE }.count).isEqualTo(0)
        assertThat(ledger.first { row -> row.kind == YourDataLedgerKind.PURCHASES }.count).isEqualTo(0)
    }

    @Test
    fun ledger_keepsTheStatusOfEveryApplicationOutOfTheCount() {
        val ledger = yourDataLedger(
            profile = canonicalCandidateProfile,
            applications = listOf(
                canonicalApplication.copy(status = ApplicationStatus.SAVED),
                canonicalApplication.copy(id = "second", status = ApplicationStatus.INTERVIEW),
            ),
            entitlement = null,
        )

        assertThat(ledger.first { row -> row.kind == YourDataLedgerKind.APPLICATIONS }.count).isEqualTo(2)
    }

    private fun seedCanonicalData() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    private fun YourDataUiState.rowOf(kind: YourDataLedgerKind): YourDataLedgerRow =
        ledger.first { row -> row.kind == kind }
}
