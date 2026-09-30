package com.hirehop.feature.tailor.impl.exported

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.exportpreview.ExportFormat
import com.hirehop.feature.tailor.impl.packpurchase.TestPaymentGateway
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

private const val APPLICATION_ID = "application-northwind-1"

private const val EXPORT_DIRECTORY = "exports"

@RunWith(AndroidJUnit4::class)
class ExportedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val assembler = ResumeDocumentAssembler()
    private val paymentGateway = TestPaymentGateway()
    private val fileStore = ExportedFileStore(ApplicationProvider.getApplicationContext())

    private lateinit var viewModel: ExportedViewModel

    @Before
    fun setup() {
        viewModel = newViewModel()
    }

    @Test
    fun loadingScenario_staysIdle() {
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.IDLE)
    }

    @Test
    fun emptyScenario_reportsNoApplication() {
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.NO_APPLICATION)
    }

    @Test
    fun missingApplication_reportsNoApplication() = runTest {
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.NO_APPLICATION)
    }

    @Test
    fun freeCreditState_namesTheJobAndSpendsTheFreeApplication() = runTest {
        given()
        paymentGateway.withFreeCredits(credits = 1)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportedStage.READY)
        assertThat(state.jobTitle).isEqualTo("Associate Android Engineer")
        assertThat(state.jobCompany).isEqualTo("Northwind GCC")
        assertThat(state.format).isEqualTo(ExportFormat.PDF)
        assertThat(state.fileName).endsWith(".pdf")
        assertThat(state.creditsKnown).isTrue()
        assertThat(state.usesFreeCredit).isTrue()
        assertThat(state.creditsBefore).isEqualTo(1)
        assertThat(state.creditsLeft).isEqualTo(0)
        assertThat(state.creditsSpent).isEqualTo(1)
    }

    @Test
    fun paidCreditState_dropsTheCounterByOne() = runTest {
        given()
        paymentGateway.withFreeCredits(credits = 0).withPurchasedCredits(credits = 5)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.usesFreeCredit).isFalse()
        assertThat(state.creditsBefore).isEqualTo(5)
        assertThat(state.creditsLeft).isEqualTo(4)
        assertThat(state.creditsSpent).isEqualTo(1)
    }

    @Test
    fun paidCreditState_claimsNoExpiryOnlyWhenEveryPackAgrees() = runTest {
        given()
        paymentGateway.withFreeCredits(credits = 0).withPurchasedCredits(credits = 5)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.creditsNeverExpire).isTrue()
    }

    @Test
    fun unavailableEntitlement_leavesTheCreditBlockOut() = runTest {
        given()
        paymentGateway.withEntitlementFailure()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportedStage.READY)
        assertThat(state.creditsKnown).isFalse()
    }

    @Test
    fun enteringTwice_keepsTheFirstLoad() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(status = ApplicationStatus.OFFER)))
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.SAVED)
    }

    @Test
    fun shareRequest_handsTheWrittenFileToTheSystem() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        val fileName = viewModel.uiState.value.fileName
        writeExportedFile(fileName)

        viewModel.onAction(ExportedAction.RequestShare)

        val request = requireNotNull(viewModel.uiState.value.shareRequest)
        assertThat(request.file.name).isEqualTo(fileName)
        assertThat(request.format).isEqualTo(ExportFormat.PDF)
        assertThat(request.jobTitle).isEqualTo("Associate Android Engineer")
        assertThat(viewModel.uiState.value.shareState).isEqualTo(ExportedShareState.REQUESTED)

        viewModel.onAction(ExportedAction.ShareHandedToSystem)

        assertThat(viewModel.uiState.value.shareRequest).isNull()
    }

    @Test
    fun shareRequest_withoutTheFileOnDevice_asksNothing() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        viewModel.uiState.value.fileName

        viewModel.onAction(ExportedAction.RequestShare)

        assertThat(viewModel.uiState.value.shareRequest).isNull()
        assertThat(viewModel.uiState.value.shareState).isEqualTo(ExportedShareState.IDLE)
    }

    @Test
    fun enteringAgain_picksUpTheFileWrittenSinceTheLastVisit() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        assertThat(viewModel.uiState.value.fileOnDevice).isFalse()

        writeExportedFile(viewModel.uiState.value.fileName)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.fileOnDevice).isTrue()
        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.READY)
    }

    @Test
    fun statusSheet_opensOnTheCurrentValue() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        viewModel.onAction(ExportedAction.OpenStatusSheet)

        assertThat(viewModel.uiState.value.statusSheetOpen).isTrue()
        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(viewModel.uiState.value.asksForStatus).isTrue()
    }

    @Test
    fun confirmStatus_writesTheStatusAndShowsTheChip() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        viewModel.onAction(ExportedAction.OpenStatusSheet)

        viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.APPLIED))

        val state = viewModel.uiState.value
        assertThat(state.status).isEqualTo(ApplicationStatus.APPLIED)
        assertThat(state.statusSheetOpen).isFalse()
        assertThat(state.statusJustSet).isTrue()
        assertThat(state.asksForStatus).isFalse()
        assertThat(state.showsStatusChip).isTrue()
        assertThat(storedStatus()).isEqualTo(ApplicationStatus.APPLIED)
    }

    @Test
    fun dismissStatusSheet_keepsTheSavedStatus() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        viewModel.onAction(ExportedAction.OpenStatusSheet)

        viewModel.onAction(ExportedAction.DismissStatusSheet)

        val state = viewModel.uiState.value
        assertThat(state.statusSheetOpen).isFalse()
        assertThat(state.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(state.statusJustSet).isFalse()
    }

    @Test
    fun confirmStatus_withoutTheSheetOpen_changesNothing() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.OFFER))

        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(storedStatus()).isEqualTo(ApplicationStatus.SAVED)
    }

    @Test
    fun docxFormat_namesTheDocxFile() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "docx", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.format).isEqualTo(ExportFormat.DOCX)
        assertThat(state.fileName).endsWith(".docx")
    }

    @Test
    fun unknownWireFormat_fallsBackToPdf() = runTest {
        given()
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "word", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.format).isEqualTo(ExportFormat.PDF)
    }

    @Test
    fun noTailoredResume_reportsNoFile() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(tailoredResume = null)))
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportedStage.NO_FILE)
        assertThat(state.fileName).isEmpty()
    }

    private fun newViewModel(): ExportedViewModel = ExportedViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        assembler = assembler,
        paymentGateway = paymentGateway,
        fileStore = fileStore,
    )

    private suspend fun storedStatus(): ApplicationStatus? =
        applicationRepository.observeApplications().first().firstOrNull()?.status

    private fun given() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    private fun writeExportedFile(fileName: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.cacheDir, EXPORT_DIRECTORY)
        directory.mkdirs()
        File(directory, fileName).writeText("%PDF-1.4")
    }
}
