package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.ExportPreviewNavKey
import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.document.TestResumeHeadings
import com.tailormyresume.feature.tailor.impl.export.RenderedResume
import com.tailormyresume.feature.tailor.impl.export.ResumePdfRenderer
import com.tailormyresume.feature.tailor.impl.export.docx.ResumeDocxRenderer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

private const val APPLICATION_ID = "application-northwind-1"

private const val PDF_NAME = "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf"

private val BATCH_SHEET = ExportPreviewSheet(
    name = "Priya Deshmukh",
    contactLine = "Pune, Maharashtra · priya.d@example.com · +91 98XXX XXXXX",
    headline = "",
    skillsHeading = "Skills",
    skills = listOf("SQL", "Advanced Excel (pivots)", "Power BI"),
    sections = listOf(
        ExportPreviewSection(
            heading = "Experience",
            entries = listOf(
                ExportPreviewEntry(
                    title = "Data Operations Associate",
                    organization = "Saffron Retail, Pune",
                    dateRange = "Jul 2025 to now",
                    bullets = listOf(
                        "Built weekly sales reports in Excel for 40 stores.",
                        "Cleaned order data with SQL for weekly reporting.",
                    ),
                ),
            ),
        ),
        ExportPreviewSection(
            heading = "Education",
            entries = listOf(
                ExportPreviewEntry(
                    title = "B.Tech Computer Science",
                    organization = "",
                    dateRange = "2024",
                    bullets = listOf("Coursework: DBMS (SQL)"),
                ),
            ),
        ),
    ),
)

private fun readyState(): ExportPreviewUiState = ExportPreviewUiState(
    stage = ExportPreviewStage.PREVIEW_READY,
    jobTitle = "Associate Analyst",
    jobCompany = "Northwind GCC",
    sheet = BATCH_SHEET,
    fileName = PDF_NAME,
    creditsKnown = true,
    freeCredits = 1,
)

private class SuspendingPdfRenderer(
    private val gate: CompletableDeferred<Unit>,
) : ResumePdfRenderer {
    var lastDocument: ResumeDocument? = null
    var lastFileName: String = ""
    var callCount: Int = 0

    override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume {
        callCount++
        lastDocument = document
        lastFileName = fileName
        gate.await()
        return RenderedResume(file = File(fileName), pageCount = 1)
    }
}

private class BatchDocxRenderer : ResumeDocxRenderer {
    var callCount: Int = 0

    override suspend fun render(document: ResumeDocument, fileName: String): File {
        callCount++
        return File(fileName)
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ExportPreviewBatchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeRule = createComposeRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val exportHistory = TestExportHistoryRepository()
    private val connectivity = TestConnectivityMonitor()
    private val clock = TestClock()
    private val paymentGateway = TestPaymentGateway()
    private val docxRenderer = BatchDocxRenderer()
    private val pendingExportStart = PendingExportStart()

    private fun newViewModel(pdf: ResumePdfRenderer): ExportPreviewViewModel = ExportPreviewViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        assembler = ResumeDocumentAssembler(TestResumeHeadings),
        pdfRenderer = pdf,
        docxRenderer = docxRenderer,
        paymentGateway = paymentGateway,
        exportHistoryRepository = exportHistory,
        connectivityMonitor = connectivity,
        pendingExportStart = pendingExportStart,
        clock = clock,
    )

    private fun enter(
        format: String = "pdf",
        scenario: DebugScenario = DebugScenario.DEFAULT,
        subject: ExportPreviewViewModel,
    ) {
        subject.onEnter(ExportPreviewNavKey(APPLICATION_ID, format, scenario))
    }

    private fun given() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    @Test
    fun firstPdfNameCarriesCompanyAndRole() = runTest {
        given()
        val viewModel = newViewModel(pdf = SuspendingPdfRenderer(CompletableDeferred()))
        enter(subject = viewModel)

        val pdfName = viewModel.uiState.value.fileName
        assertThat(pdfName).endsWith(".pdf")
        assertThat(pdfName).contains("Northwind_GCC")
        assertThat(pdfName).contains("Associate_Android_Engineer")

        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.DOCX))

        assertThat(viewModel.uiState.value.fileName).isEqualTo(pdfName.replace(".pdf", ".docx"))
    }

    @Test
    fun cancelWhileMakingTheFileReturnsToPreviewWithoutCharge() = runTest {
        given()
        val gate = CompletableDeferred<Unit>()
        val renderer = SuspendingPdfRenderer(gate)
        val viewModel = newViewModel(pdf = renderer)
        enter(subject = viewModel)
        val freeBefore = paymentGateway.entitlement().freeCredits

        viewModel.onAction(ExportPreviewAction.Export)
        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORTING)

        viewModel.onAction(ExportPreviewAction.CancelExport)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(viewModel.uiState.value.navigation).isNull()
        assertThat(paymentGateway.entitlement().freeCredits).isEqualTo(freeBefore)
        assertThat(exportHistory.observeExports(APPLICATION_ID).first()).isEmpty()
    }

    @Test
    fun exportingSheetOffersCancel() {
        var cancels = 0
        composeRule.setContent {
            ExportPreviewHost(
                uiState = readyState().copy(stage = ExportPreviewStage.EXPORTING),
                onCancel = { cancels++ },
            )
        }

        composeRule.onNodeWithText("Nothing is charged until the file is ready.").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeRule.onAllNodesWithText("Download tapped", substring = true).assertCountEquals(0)

        composeRule.onNodeWithText("Cancel").performClick()

        assertThat(cancels).isEqualTo(1)
    }

    @Test
    fun captionReadsFormatAndPlain() {
        composeRule.setContent { ExportPreviewHost(uiState = readyState(), onCancel = {}) }

        composeRule.onNodeWithText("PDF · Plain").assertIsDisplayed()
        composeRule.onAllNodesWithText("ATS", substring = true).assertCountEquals(0)
    }
}

@Composable
private fun ExportPreviewHost(
    uiState: ExportPreviewUiState,
    onCancel: () -> Unit,
) {
    TmrTheme {
        ExportPreviewScreen(
            uiState = uiState,
            actions = ExportPreviewActions(
                onSelectFormat = {},
                onExport = {},
                onRetry = {},
                onNavigateBack = {},
                onBuyCredits = {},
                onCancel = onCancel,
            ),
        )
    }
}
