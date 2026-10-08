package com.tailormyresume.feature.tailor.impl.exported

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.ExportedNavKey
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.document.TestResumeHeadings
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
    private val paymentGateway = TestPaymentGateway()
    private val exportHistory = TestExportHistoryRepository()
    private val clock = TestClock()
    private val fileStore = ExportedFileStore(ApplicationProvider.getApplicationContext())

    private lateinit var viewModel: ExportedViewModel

    @Before
    fun setup() {
        viewModel = ExportedViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            assembler = ResumeDocumentAssembler(TestResumeHeadings),
            paymentGateway = paymentGateway,
            exportHistoryRepository = exportHistory,
            fileStore = fileStore,
            clock = clock,
        )
    }

    private fun enter(format: String = "pdf", scenario: DebugScenario = DebugScenario.DEFAULT, free: Boolean = true) {
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, format, scenario, spentFreeCredit = free))
    }

    @Test
    fun loadingScenario_staysLoading() {
        enter(scenario = DebugScenario.LOADING)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.LOADING)
    }

    @Test
    fun emptyScenario_reportsNoApplication() {
        enter(scenario = DebugScenario.EMPTY)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.NO_APPLICATION)
    }

    @Test
    fun missingApplication_reportsNoApplication() = runTest {
        enter()

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.NO_APPLICATION)
    }

    @Test
    fun freeCredit_namesTheJobAndReadsTheCreditsLeftFromTheGateway() = runTest {
        given()
        paymentGateway.unlock("application-1")
        enter(free = true)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportedStage.READY)
        assertThat(state.jobTitle).isEqualTo("Associate Android Engineer")
        assertThat(state.jobCompany).isEqualTo("Northwind GCC")
        assertThat(state.fileName).endsWith(".pdf")
        assertThat(state.creditsKnown).isTrue()
        assertThat(state.usesFreeCredit).isTrue()
        assertThat(state.creditsLeft).isEqualTo(0)
    }

    @Test
    fun theExportRecord_givesTheFileNamePageCountAndTemplate() = runTest {
        given()
        exportHistory.record(
            ExportRecord(
                applicationId = APPLICATION_ID,
                format = ExportFormat.PDF,
                fileName = "Recorded_Name.pdf",
                exportedAt = clock.now(),
                creditKind = CreditKind.FREE,
                pageCount = 1,
                templateName = "Plain",
            ),
        )
        enter()

        val state = viewModel.uiState.value
        assertThat(state.fileName).isEqualTo("Recorded_Name.pdf")
        assertThat(state.pageCount).isEqualTo(1)
        assertThat(state.templateName).isEqualTo("Plain")
    }

    @Test
    fun withoutAnExportRecord_pageCountAndTemplateStayUnknown() = runTest {
        given()
        enter()

        assertThat(viewModel.uiState.value.pageCount).isNull()
        assertThat(viewModel.uiState.value.templateName).isNull()
    }

    @Test
    fun paidCredit_dropsTheCounterByOne() = runTest {
        given()
        paymentGateway.unlock("application-2")
        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        paymentGateway.unlock("application-3")
        enter(free = false)

        val state = viewModel.uiState.value
        assertThat(state.usesFreeCredit).isFalse()
        assertThat(state.creditsBefore).isEqualTo(5)
        assertThat(state.creditsLeft).isEqualTo(4)
        assertThat(state.creditsNeverExpire).isTrue()
    }

    @Test
    fun theCounterFollowsAnyLaterChangeOfTheCredits() = runTest {
        given()
        paymentGateway.unlock("application-4")
        enter(free = true)

        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(viewModel.uiState.value.creditsLeft).isEqualTo(5)
    }

    @Test
    fun enteringTwice_keepsTheFirstLoad() = runTest {
        given()
        enter()
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(status = ApplicationStatus.OFFER)))
        enter()

        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.SAVED)
    }

    @Test
    fun shareRequest_handsTheWrittenFileToTheSystem() = runTest {
        given()
        enter()
        val fileName = viewModel.uiState.value.fileName
        writeExportedFile(fileName)

        viewModel.onAction(ExportedAction.RequestShare)

        val request = requireNotNull(viewModel.uiState.value.fileRequest)
        assertThat(request.file.name).isEqualTo(fileName)
        assertThat(request.format).isEqualTo(ExportFormat.PDF)
        assertThat(request.action).isEqualTo(ExportedFileAction.SHARE)

        viewModel.onAction(ExportedAction.FileRequestHandled)

        assertThat(viewModel.uiState.value.fileRequest).isNull()
    }

    @Test
    fun openRequest_handsTheWrittenFileToTheSystem() = runTest {
        given()
        enter()
        writeExportedFile(viewModel.uiState.value.fileName)

        viewModel.onAction(ExportedAction.RequestOpen)

        assertThat(viewModel.uiState.value.fileRequest?.action).isEqualTo(ExportedFileAction.OPEN)
    }

    @Test
    fun fileRequest_withoutTheFileOnDevice_asksNothing() = runTest {
        given()
        enter()

        viewModel.onAction(ExportedAction.RequestShare)
        viewModel.onAction(ExportedAction.RequestOpen)

        assertThat(viewModel.uiState.value.fileRequest).isNull()
        assertThat(viewModel.uiState.value.canUseFile).isFalse()
    }

    @Test
    fun enteringAgain_picksUpTheFileWrittenSinceTheLastVisit() = runTest {
        given()
        enter()
        assertThat(viewModel.uiState.value.fileOnDevice).isFalse()

        writeExportedFile(viewModel.uiState.value.fileName)
        enter()

        assertThat(viewModel.uiState.value.fileOnDevice).isTrue()
    }

    @Test
    fun confirmStatus_writesTheStatusMarksTheDateAndOffersUndo() = runTest {
        given()
        enter()
        viewModel.onAction(ExportedAction.OpenStatusSheet)

        viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.APPLIED))

        val state = viewModel.uiState.value
        assertThat(state.status).isEqualTo(ApplicationStatus.APPLIED)
        assertThat(state.statusSheetOpen).isFalse()
        assertThat(state.asksForStatus).isFalse()
        assertThat(state.markedOn).isNotEmpty()
        assertThat(state.undoStatus).isEqualTo(ApplicationStatus.SAVED)
        assertThat(storedStatus()).isEqualTo(ApplicationStatus.APPLIED)
    }

    @Test
    fun undo_restoresTheEarlierStatusInTheStore() = runTest {
        given()
        enter()
        viewModel.onAction(ExportedAction.OpenStatusSheet)
        viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.APPLIED))

        viewModel.onAction(ExportedAction.UndoStatus)

        val state = viewModel.uiState.value
        assertThat(state.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(state.undoStatus).isNull()
        assertThat(state.markedOn).isNull()
        assertThat(storedStatus()).isEqualTo(ApplicationStatus.SAVED)
    }

    @Test
    fun dismissUndo_keepsTheNewStatus() = runTest {
        given()
        enter()
        viewModel.onAction(ExportedAction.OpenStatusSheet)
        viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.INTERVIEW))

        viewModel.onAction(ExportedAction.DismissUndo)

        assertThat(viewModel.uiState.value.undoStatus).isNull()
        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.INTERVIEW)
    }

    @Test
    fun dismissStatusSheet_keepsTheSavedStatus() = runTest {
        given()
        enter()
        viewModel.onAction(ExportedAction.OpenStatusSheet)

        viewModel.onAction(ExportedAction.DismissStatusSheet)

        assertThat(viewModel.uiState.value.statusSheetOpen).isFalse()
        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(viewModel.uiState.value.undoStatus).isNull()
    }

    @Test
    fun confirmStatus_withoutTheSheetOpen_changesNothing() = runTest {
        given()
        enter()

        viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.OFFER))

        assertThat(viewModel.uiState.value.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(storedStatus()).isEqualTo(ApplicationStatus.SAVED)
    }

    @Test
    fun docxFormat_namesTheDocxFile_andUnknownFormatFallsBackToPdf() = runTest {
        given()
        enter(format = "docx")
        assertThat(viewModel.uiState.value.format).isEqualTo(ExportFormat.DOCX)
        assertThat(viewModel.uiState.value.fileName).endsWith(".docx")
    }

    @Test
    fun noTailoredResume_reportsNoFile() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(tailoredResume = null)))
        profileRepository.sendProfile(canonicalCandidateProfile)
        enter()

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportedStage.NO_FILE)
        assertThat(viewModel.uiState.value.fileName).isEmpty()
    }

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
