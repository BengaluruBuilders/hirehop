package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.export.ResumePdfRenderer
import com.hirehop.feature.tailor.impl.export.docx.ResumeDocxRenderer
import com.hirehop.feature.tailor.impl.packpurchase.TestPaymentGateway
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private const val APPLICATION_ID = "application-northwind-1"

private class PreviewOnlyRenderer : ResumePdfRenderer, ResumeDocxRenderer {
    override suspend fun render(
        document: com.hirehop.feature.tailor.impl.document.ResumeDocument,
        fileName: String,
    ): File = File(fileName)
}

private class SuspendingRenderer : ResumePdfRenderer, ResumeDocxRenderer {
    override suspend fun render(
        document: com.hirehop.feature.tailor.impl.document.ResumeDocument,
        fileName: String,
    ): File {
        awaitCancellation()
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ExportPreviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val renderer = PreviewOnlyRenderer()
    private val paymentGateway = TestPaymentGateway()

    @Test
    fun rendering_readsAsAnUnsetPageNotAFailure() {
        capture("ExportPreviewRendering", viewModelFor(DebugScenario.LOADING))
    }

    @Test
    fun pdfSelected_readsTheWholeDocumentAsAPreview() {
        capture("ExportPreviewPdfSelected", viewModelFor(DebugScenario.DEFAULT))
    }

    @Test
    fun docxSelected_namesTheDocxFileAndKeepsTheSameText() {
        capture("ExportPreviewDocxSelected", viewModelFor(DebugScenario.DEFAULT, format = "docx"))
    }

    @Test
    fun offline_keepsThePreviewReadableWithTheDownloadOff() {
        capture("ExportPreviewOffline", viewModelFor(DebugScenario.OFFLINE))
    }

    @Test
    fun previewFailed_saysSoAndShowsNoDocument() {
        capture("ExportPreviewFailed", viewModelFor(DebugScenario.ERROR))
    }

    @Test
    fun noDocument_asksTheUserToReviewFirst() {
        capture("ExportPreviewNoDocument", viewModelFor(DebugScenario.EMPTY))
    }

    @Test
    fun exporting_saysTheFileIsBeingWrittenOnThisDevice() {
        given()
        val stalled = newViewModel(pdf = SuspendingRenderer(), docx = SuspendingRenderer())
        stalled.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.EXPORTING))
        capture("ExportPreviewExporting", stalled)
    }

    @Test
    fun exported_saysTheFileIsWrittenAndNotYetShared() {
        capture("ExportPreviewExported", exportedState())
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun pdfSelected_atLargeTextKeepsTheFormatSelectorUsable() {
        capture(
            "ExportPreviewPdfSelectedFont200",
            viewModelFor(DebugScenario.DEFAULT),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun capture(
        screenName: String,
        viewModel: ExportPreviewViewModel,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            ExportPreviewHost(uiState = viewModel.uiState.value, dark = darkTheme.value)
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }

    private fun viewModelFor(scenario: DebugScenario, format: String = "pdf"): ExportPreviewViewModel {
        given()
        val viewModel = newViewModel()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, format, scenario))
        return viewModel
    }

    private fun exportedState(): ExportPreviewViewModel {
        given()
        val viewModel = newViewModel()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        viewModel.onAction(ExportPreviewAction.Export)
        return viewModel
    }

    private fun newViewModel(
        pdf: ResumePdfRenderer = renderer,
        docx: ResumeDocxRenderer = renderer,
    ): ExportPreviewViewModel = ExportPreviewViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        assembler = ResumeDocumentAssembler(),
        pdfRenderer = pdf,
        docxRenderer = docx,
        paymentGateway = paymentGateway,
    )

    private fun given(gap: GapAnalysis = requireNotNull(canonicalApplication.gapAnalysis)) {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(gapAnalysis = gap)))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }
}

@Composable
private fun ExportPreviewHost(
    uiState: ExportPreviewUiState,
    dark: Boolean,
) {
    com.hirehop.core.designsystem.theme.HhTheme(darkTheme = dark) {
        ExportPreviewScreen(
            uiState = uiState,
            actions = ExportPreviewActions(
                onSelectFormat = {},
                onExport = {},
                onRetryPreview = {},
                onDismissResult = {},
                onNavigateBack = {},
                onExported = {},
                onBuyCredits = {},
            ),
        )
    }
}
