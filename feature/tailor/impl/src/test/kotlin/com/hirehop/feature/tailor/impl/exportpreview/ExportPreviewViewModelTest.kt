package com.hirehop.feature.tailor.impl.exportpreview

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.canonicalProfileWithoutEntries
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.export.ResumePdfRenderer
import com.hirehop.feature.tailor.impl.export.docx.ResumeDocxRenderer
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException

private const val APPLICATION_ID = "application-northwind-1"

private class RecordingPdfRenderer(
    private val failure: IOException? = null,
) : ResumePdfRenderer {
    var lastDocument: ResumeDocument? = null
    var lastFileName: String = ""
    var callCount: Int = 0

    override suspend fun render(document: ResumeDocument, fileName: String): File {
        callCount++
        lastDocument = document
        lastFileName = fileName
        failure?.let { problem -> throw problem }
        return File(fileName)
    }
}

private class RecordingDocxRenderer : ResumeDocxRenderer {
    var lastDocument: ResumeDocument? = null
    var lastFileName: String = ""
    var callCount: Int = 0

    override suspend fun render(document: ResumeDocument, fileName: String): File {
        callCount++
        lastDocument = document
        lastFileName = fileName
        return File(fileName)
    }
}

class ExportPreviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val assembler = ResumeDocumentAssembler()
    private val pdfRenderer = RecordingPdfRenderer()
    private val docxRenderer = RecordingDocxRenderer()

    private lateinit var viewModel: ExportPreviewViewModel

    @Before
    fun setup() {
        viewModel = newViewModel(pdf = pdfRenderer, docx = docxRenderer)
    }

    @Test
    fun loadingScenario_staysOnRenderingWithoutAskingTheDomain() {
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.LOADING))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.RENDERING)
        assertThat(state.sheet).isNull()
    }

    @Test
    fun errorScenario_reportsAFailedPreview() {
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.ERROR))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.PREVIEW_FAILED)
        assertThat(state.sheet).isNull()
    }

    @Test
    fun defaultScenario_previewsTheDocumentTheRendererWillWrite() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(state.jobTitle).isEqualTo("Associate Android Engineer")
        assertThat(state.jobCompany).isEqualTo("Northwind GCC")
        assertThat(state.hasSheet).isTrue()
        assertThat(state.canExport).isTrue()
    }

    @Test
    fun previewAndExport_shareTheSameDocumentInstance() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        val previewed = viewModel.uiState.value.sheet

        viewModel.onAction(ExportPreviewAction.Export)

        val written = requireNotNull(pdfRenderer.lastDocument)
        assertThat(written).isSameInstanceAs(pdfRenderer.lastDocument)
        assertThat(previewed?.name).isEqualTo(written.name)
        assertThat(previewed?.sections?.map { section -> section.heading })
            .isEqualTo(written.sections.map { section -> section.heading })
        assertThat(previewed?.sections?.flatMap { section -> section.entries.flatMap { entry -> entry.bullets } })
            .isEqualTo(
                written.sections.flatMap { section -> section.entries.flatMap { entry -> entry.bullets } },
            )
    }

    @Test
    fun exportWritesTheFileNameThePreviewNamed() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        val named = viewModel.uiState.value.fileName
        assertThat(named).endsWith(".pdf")

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(pdfRenderer.lastFileName).isEqualTo(named)
    }

    @Test
    fun selectDocx_renamesTheFileAndUsesTheDocxRenderer() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.DOCX))
        val state = viewModel.uiState.value
        assertThat(state.format).isEqualTo(ExportFormat.DOCX)
        assertThat(state.fileName).endsWith(".docx")
        assertThat(state.fileName).doesNotContain(".pdf")

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(docxRenderer.callCount).isEqualTo(1)
        assertThat(pdfRenderer.callCount).isEqualTo(0)
    }

    @Test
    fun selectDocx_keepsTheSameTextInThePreview() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        val before = viewModel.uiState.value.sheet

        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.DOCX))

        assertThat(viewModel.uiState.value.sheet).isEqualTo(before)
    }

    @Test
    fun selectTheSameFormatTwice_keepsTheSelectedState() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        val before = viewModel.uiState.value

        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.PDF))

        assertThat(viewModel.uiState.value).isEqualTo(before)
    }

    @Test
    fun navKeyCarriesTheDocxFormat() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "docx", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.format).isEqualTo(ExportFormat.DOCX)
    }

    @Test
    fun unknownNavKeyFormatFallsBackToPdf() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "xlsx", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.format).isEqualTo(ExportFormat.PDF)
    }

    @Test
    fun exportSucceeds_reportsTheWrittenFormat() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        viewModel.onAction(ExportPreviewAction.Export)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.EXPORT_SUCCEEDED)
        assertThat(state.exportedFormat).isEqualTo(ExportFormat.PDF)
    }

    @Test
    fun exportFails_reportsAFailureAndWritesNothing() = runTest {
        given()
        val failing = newViewModel(pdf = RecordingPdfRenderer(failure = IOException("no space")), docx = docxRenderer)
        failing.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        failing.onAction(ExportPreviewAction.Export)

        val state = failing.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.EXPORT_FAILED)
        assertThat(state.exportedFormat).isNull()
        assertThat(state.hasSheet).isTrue()
    }

    @Test
    fun exportFailsAndRetries_succeedsWithoutLeavingTheFailedState() = runTest {
        given()
        val failing = newViewModel(pdf = RecordingPdfRenderer(failure = IOException("no space")), docx = docxRenderer)
        failing.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        failing.onAction(ExportPreviewAction.Export)
        assertThat(failing.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORT_FAILED)

        val recovered = newViewModel(pdf = pdfRenderer, docx = docxRenderer)
        recovered.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        recovered.onAction(ExportPreviewAction.Export)

        assertThat(recovered.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORT_SUCCEEDED)
    }

    @Test
    fun offlineScenario_keepsThePreviewReadableAndBlocksTheDownload() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.OFFLINE))

        val state = viewModel.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.stage).isEqualTo(ExportPreviewStage.OFFLINE)
        assertThat(state.hasSheet).isTrue()
        assertThat(state.canExport).isFalse()
    }

    @Test
    fun offlineScenario_exportActionIsIgnored() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.OFFLINE))

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(pdfRenderer.callCount).isEqualTo(0)
        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.OFFLINE)
    }

    @Test
    fun noDocument_asksTheUserToReviewFirst() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalProfileWithoutEntries)
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
        assertThat(state.sheet).isNull()
        assertThat(state.canExport).isFalse()
    }

    @Test
    fun noTailoredResume_asksTheUserToReviewFirst() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(tailoredResume = null)))
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
    }

    @Test
    fun emptyScenario_isTheNoDocumentState() {
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
    }

    @Test
    fun missingApplication_isAPreviewFailure() = runTest {
        applicationRepository.sendApplications(emptyList())
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_FAILED)
    }

    @Test
    fun previewFailure_showsNoPreviewBesideTheDownload() {
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.ERROR))

        val state = viewModel.uiState.value
        assertThat(state.sheet).isNull()
        assertThat(state.canExport).isFalse()
    }

    @Test
    fun retryAfterAFailure_rendersThePreviewAgain() = runTest {
        given()
        val failing = ExportPreviewViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            assembler = assembler,
            pdfRenderer = pdfRenderer,
            docxRenderer = docxRenderer,
        )
        failing.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.ERROR))
        assertThat(failing.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_FAILED)

        failing.onAction(ExportPreviewAction.RetryPreview)

        assertThat(failing.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(failing.uiState.value.hasSheet).isTrue()
    }

    @Test
    fun exportingScenario_exportsAsSoonAsThePreviewIsReady() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.EXPORTING))

        assertThat(pdfRenderer.callCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORT_SUCCEEDED)
    }

    @Test
    fun successScenario_isAnOrdinaryReadyPreview() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.SUCCESS))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(pdfRenderer.callCount).isEqualTo(0)
    }

    @Test
    fun partialScenario_isAnOrdinaryReadyPreview() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.PARTIAL))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
    }

    @Test
    fun everyScenarioMapsToAKnownStage() = runTest {
        for (scenario in DebugScenario.entries) {
            given()
            val fresh = newViewModel()
            fresh.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", scenario))

            assertThat(fresh.uiState.value.stage).isIn(ExportPreviewStage.entries.toList())
        }
    }

    @Test
    fun everyScenarioNeverShowsAPreviewWithoutADocument() = runTest {
        for (scenario in DebugScenario.entries) {
            applicationRepository.sendApplications(emptyList())
            val fresh = newViewModel()
            fresh.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", scenario))

            assertThat(fresh.uiState.value.sheet).isNull()
            assertThat(fresh.uiState.value.canExport).isFalse()
        }
    }

    @Test
    fun previewCarriesNoScaleOrTimeSavingClaim() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val sheet = requireNotNull(viewModel.uiState.value.sheet)
        val text = buildString {
            append(sheet.name)
            append(sheet.contactLine)
            append(sheet.headline)
            append(sheet.skills.joinToString())
            sheet.sections.forEach { section ->
                append(section.heading)
                section.entries.forEach { entry ->
                    append(entry.title)
                    append(entry.organization)
                    append(entry.dateRange)
                    append(entry.bullets.joinToString())
                }
            }
        }.lowercase()
        assertThat(text).doesNotContain("guarantee")
        assertThat(text).doesNotContain("hours saved")
        assertThat(text).doesNotContain("shortlist")
        assertThat(text).doesNotMatch("\\bats score\\b")
        assertThat(text).doesNotMatch("\\bats[- ]proof\\b")
        assertThat(text).doesNotMatch("\\bget(s)? interviews?\\b")
        assertThat(text).doesNotMatch("\\bwill get you\\b")
    }

    @Test
    fun previewShowsOnlyConfirmedFacts() = runTest {
        given()
        val unconfirmed = canonicalCandidateProfile.copy(
            entries = canonicalCandidateProfile.entries.map { entry -> entry.copy(isConfirmed = false) },
        )
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(unconfirmed)
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
    }

    @Test
    fun onEnter_ignoresASecondKey() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        applicationRepository.sendApplications(emptyList())

        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
    }

    @Test
    fun dismissResult_returnsToThePreview() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        viewModel.onAction(ExportPreviewAction.Export)

        viewModel.onAction(ExportPreviewAction.DismissResult)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(state.exportedFormat).isNull()
    }

    @Test
    fun fileName_isBuiltFromTheNameCompanyAndRole() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val name = viewModel.uiState.value.fileName
        assertThat(name).startsWith("Priya_Deshmukh")
        assertThat(name).contains("Northwind_GCC")
        assertThat(name).contains("Associate_Android_Engineer")
    }

    @Test
    fun docxFileName_dropsThePdfExtension() {
        val pdf = ExportFileNames.build(ExportFormat.PDF, "A B", "C D", "E F")
        val docx = ExportFileNames.build(ExportFormat.DOCX, "A B", "C D", "E F")

        assertThat(pdf).endsWith(".pdf")
        assertThat(docx).endsWith(".docx")
        assertThat(docx).isEqualTo(pdf.removeSuffix(".pdf") + ".docx")
    }

    @Test
    fun formatFromWire_ignoresCase() {
        assertThat(ExportFormat.fromWire("PDF")).isEqualTo(ExportFormat.PDF)
        assertThat(ExportFormat.fromWire("DocX")).isEqualTo(ExportFormat.DOCX)
        assertThat(ExportFormat.fromWire("")).isEqualTo(ExportFormat.PDF)
    }

    @Test
    fun lineCount_countsEveryRenderedLine() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        val sheet = requireNotNull(viewModel.uiState.value.sheet)
        assertThat(sheet.lineCount).isAtLeast(sheet.sections.size)
        assertThat(sheet.lineCount).isGreaterThan(0)
    }

    @Test
    fun sheetProjection_keepsEveryFieldOfTheDocument() = runTest {
        given()
        viewModel.onEnter(ExportPreviewNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        val document = requireNotNull(assembler.assemble(canonicalCandidateProfile, requireNotNull(canonicalApplication.tailoredResume)))

        val sheet = requireNotNull(viewModel.uiState.value.sheet)

        assertThat(sheet.name).isEqualTo(document.name)
        assertThat(sheet.contactLine).isEqualTo(document.contactLine)
        assertThat(sheet.headline).isEqualTo(document.headline)
        assertThat(sheet.skills).isEqualTo(document.skills)
        assertThat(sheet.sections.map { section -> section.heading })
            .isEqualTo(document.sections.map { section -> section.heading })
    }

    private fun newViewModel(
        pdf: ResumePdfRenderer = pdfRenderer,
        docx: ResumeDocxRenderer = docxRenderer,
    ): ExportPreviewViewModel = ExportPreviewViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        assembler = assembler,
        pdfRenderer = pdf,
        docxRenderer = docx,
    )

    private fun given() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }
}
