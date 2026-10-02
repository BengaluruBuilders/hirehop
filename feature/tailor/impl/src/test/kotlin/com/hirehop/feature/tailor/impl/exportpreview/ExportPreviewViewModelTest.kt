package com.hirehop.feature.tailor.impl.exportpreview

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.model.CreditKind
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.canonicalProfileWithoutEntries
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestExportHistoryRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestClock
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.impl.document.ExportTemplate
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.document.TestResumeHeadings
import com.hirehop.feature.tailor.impl.export.RenderedResume
import com.hirehop.feature.tailor.impl.export.ResumePdfRenderer
import com.hirehop.feature.tailor.impl.export.docx.ResumeDocxRenderer
import kotlinx.coroutines.flow.first
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
    var pageCount: Int = 1

    override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume {
        callCount++
        lastDocument = document
        lastFileName = fileName
        failure?.let { problem -> throw problem }
        return RenderedResume(file = File(fileName), pageCount = pageCount)
    }
}

private class RecordingDocxRenderer : ResumeDocxRenderer {
    var lastDocument: ResumeDocument? = null
    var callCount: Int = 0

    override suspend fun render(document: ResumeDocument, fileName: String): File {
        callCount++
        lastDocument = document
        return File(fileName)
    }
}

class ExportPreviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val exportHistory = TestExportHistoryRepository()
    private val connectivity = TestConnectivityMonitor()
    private val clock = TestClock()
    private val paymentGateway = TestPaymentGateway()
    private val pdfRenderer = RecordingPdfRenderer()
    private val docxRenderer = RecordingDocxRenderer()
    private val pendingExportStart = PendingExportStart()

    private lateinit var viewModel: ExportPreviewViewModel

    @Before
    fun setup() {
        viewModel = newViewModel()
    }

    private fun newViewModel(pdf: ResumePdfRenderer = pdfRenderer): ExportPreviewViewModel = ExportPreviewViewModel(
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
        subject: ExportPreviewViewModel = viewModel,
    ) {
        subject.onEnter(ExportPreviewNavKey(APPLICATION_ID, format, scenario))
    }

    private fun given() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    @Test
    fun loadingScenario_staysOnRenderingWithoutLoadingADocument() {
        enter(scenario = DebugScenario.LOADING)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.RENDERING)
        assertThat(viewModel.uiState.value.sheet).isNull()
    }

    @Test
    fun errorScenario_reportsAFailedPreview() {
        enter(scenario = DebugScenario.ERROR)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_FAILED)
        assertThat(viewModel.uiState.value.canExport).isFalse()
    }

    @Test
    fun emptyScenario_isTheNoDocumentState() {
        enter(scenario = DebugScenario.EMPTY)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
    }

    @Test
    fun defaultScenario_previewsTheDocumentTheRendererWillWrite() = runTest {
        given()
        enter()

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(state.jobTitle).isEqualTo("Associate Android Engineer")
        assertThat(state.jobCompany).isEqualTo("Northwind GCC")
        assertThat(state.sheet).isNotNull()
        assertThat(state.canExport).isTrue()
    }

    @Test
    fun sectionHeadingsComeFromTheHeadingsProvider() = runTest {
        given()
        enter()

        val headings = requireNotNull(viewModel.uiState.value.sheet).sections.map { section -> section.heading }
        assertThat(headings).isNotEmpty()
        assertThat(headings.all { heading -> heading.startsWith("Heading ") }).isTrue()
        assertThat(viewModel.uiState.value.sheet?.skillsHeading).isEqualTo("Heading skills")
    }

    @Test
    fun exportWritesTheSameTextTheSheetShows() = runTest {
        given()
        enter()
        val sheet = requireNotNull(viewModel.uiState.value.sheet)

        viewModel.onAction(ExportPreviewAction.Export)

        val written = requireNotNull(pdfRenderer.lastDocument)
        assertThat(written.name).isEqualTo(sheet.name)
        assertThat(written.sections.map { section -> section.heading })
            .isEqualTo(sheet.sections.map { section -> section.heading })
        assertThat(written.sections.flatMap { section -> section.entries.flatMap { entry -> entry.bullets } })
            .isEqualTo(sheet.sections.flatMap { section -> section.entries.flatMap { entry -> entry.bullets } })
    }

    @Test
    fun exportWritesTheFileNameThePreviewNamed() = runTest {
        given()
        enter()
        val named = viewModel.uiState.value.fileName
        assertThat(named).endsWith(".pdf")

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(pdfRenderer.lastFileName).isEqualTo(named)
    }

    @Test
    fun selectDocx_renamesTheFileAndUsesTheDocxRenderer() = runTest {
        given()
        enter()

        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.DOCX))
        assertThat(viewModel.uiState.value.format).isEqualTo(ExportFormat.DOCX)
        assertThat(viewModel.uiState.value.fileName).endsWith(".docx")

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(docxRenderer.callCount).isEqualTo(1)
        assertThat(pdfRenderer.callCount).isEqualTo(0)
    }

    @Test
    fun selectDocx_keepsTheSameTextInThePreview() = runTest {
        given()
        enter()
        val before = viewModel.uiState.value.sheet

        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.DOCX))

        assertThat(viewModel.uiState.value.sheet).isEqualTo(before)
    }

    @Test
    fun selectTemplate_keepsTheWordsAndPassesTheTemplateToTheRenderer() = runTest {
        given()
        enter()
        val before = viewModel.uiState.value.sheet

        viewModel.onAction(ExportPreviewAction.SelectTemplate(ExportTemplate.COMPACT))
        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(viewModel.uiState.value.template).isEqualTo(ExportTemplate.COMPACT)
        assertThat(viewModel.uiState.value.sheet).isEqualTo(before)
        assertThat(pdfRenderer.lastDocument?.template).isEqualTo(ExportTemplate.COMPACT)
    }

    @Test
    fun navKeyCarriesTheDocxFormat_andUnknownFormatsFallBackToPdf() = runTest {
        given()
        enter(format = "docx")
        assertThat(viewModel.uiState.value.format).isEqualTo(ExportFormat.DOCX)

        val other = newViewModel()
        enter(format = "xlsx", subject = other)
        assertThat(other.uiState.value.format).isEqualTo(ExportFormat.PDF)
    }

    @Test
    fun exportWithAFreeCredit_spendsItRecordsTheExportAndOpensExported() = runTest {
        given()
        enter()

        viewModel.onAction(ExportPreviewAction.Export)

        val navigation = viewModel.uiState.value.navigation
        assertThat(navigation).isEqualTo(ExportPreviewNavigation.Exported(ExportFormat.PDF, spentFreeCredit = true))
        assertThat(paymentGateway.entitlement().totalCredits).isEqualTo(0)
        val record = exportHistory.observeExports(APPLICATION_ID).first().single()
        assertThat(record.creditKind).isEqualTo(CreditKind.FREE)
        assertThat(record.format).isEqualTo(ExportFormat.PDF)
        assertThat(record.fileName).isEqualTo(pdfRenderer.lastFileName)
        assertThat(record.exportedAt).isEqualTo(clock.now())
    }

    @Test
    fun exportRecordsThePageCountAndTheTemplateName() = runTest {
        given()
        pdfRenderer.pageCount = 2
        enter()
        viewModel.onAction(ExportPreviewAction.SelectTemplate(ExportTemplate.COMPACT))

        viewModel.onAction(ExportPreviewAction.Export)

        val record = exportHistory.observeExports(APPLICATION_ID).first().single()
        assertThat(record.pageCount).isEqualTo(2)
        assertThat(record.templateName).isEqualTo("Compact")
    }

    @Test
    fun docxExportRecordsTheTemplateNameAndNoPageCount() = runTest {
        given()
        enter(format = "docx")

        viewModel.onAction(ExportPreviewAction.Export)

        val record = exportHistory.observeExports(APPLICATION_ID).first().single()
        assertThat(record.format).isEqualTo(ExportFormat.DOCX)
        assertThat(record.pageCount).isNull()
        assertThat(record.templateName).isEqualTo("Plain")
    }

    @Test
    fun aPendingExportStartForThisApplication_exportsByItselfOnce() = runTest {
        given()
        paymentGateway.consumeCredit()
        enter()
        viewModel.onAction(ExportPreviewAction.Export)
        assertThat(viewModel.uiState.value.navigation).isEqualTo(ExportPreviewNavigation.BuyCredits)
        viewModel.onAction(ExportPreviewAction.NavigationHandled)
        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        pendingExportStart.request(APPLICATION_ID)

        assertThat(pdfRenderer.callCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.navigation).isInstanceOf(ExportPreviewNavigation.Exported::class.java)
        assertThat(pendingExportStart.applicationId.value).isNull()
    }

    @Test
    fun aPendingExportStartForAnotherApplication_isLeftAlone() = runTest {
        given()
        enter()

        pendingExportStart.request("another-application")

        assertThat(pdfRenderer.callCount).isEqualTo(0)
        assertThat(pendingExportStart.applicationId.value).isEqualTo("another-application")
    }

    @Test
    fun withoutAPendingExportStart_returningFromThePackDoesNotExport() = runTest {
        given()
        paymentGateway.consumeCredit()
        enter()
        viewModel.onAction(ExportPreviewAction.Export)
        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(pdfRenderer.callCount).isEqualTo(0)
    }

    @Test
    fun exportWithAPurchasedCredit_recordsThePurchasedKind() = runTest {
        given()
        paymentGateway.consumeCredit()
        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        enter()

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(viewModel.uiState.value.navigation)
            .isEqualTo(ExportPreviewNavigation.Exported(ExportFormat.PDF, spentFreeCredit = false))
        assertThat(exportHistory.observeExports(APPLICATION_ID).first().single().creditKind)
            .isEqualTo(CreditKind.PURCHASED)
        assertThat(paymentGateway.entitlement().purchasedCredits).isEqualTo(4)
    }

    @Test
    fun withNoCredit_exportOpensThePackAndWritesNothing() = runTest {
        given()
        paymentGateway.consumeCredit()
        enter()
        assertThat(viewModel.uiState.value.needsCredits).isTrue()

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(viewModel.uiState.value.navigation).isEqualTo(ExportPreviewNavigation.BuyCredits)
        assertThat(pdfRenderer.callCount).isEqualTo(0)
        assertThat(exportHistory.observeExports(APPLICATION_ID).first()).isEmpty()
    }

    @Test
    fun creditsFollowThePurchaseMadeOnTheNextScreen() = runTest {
        given()
        paymentGateway.consumeCredit()
        enter()
        assertThat(viewModel.uiState.value.needsCredits).isTrue()

        paymentGateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(viewModel.uiState.value.needsCredits).isFalse()
        assertThat(viewModel.uiState.value.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun navigationHandled_clearsTheOneShotTarget() = runTest {
        given()
        enter()
        viewModel.onAction(ExportPreviewAction.Export)

        viewModel.onAction(ExportPreviewAction.NavigationHandled)

        assertThat(viewModel.uiState.value.navigation).isNull()
    }

    @Test
    fun renderFailure_chargesNothingRecordsNothingAndOffersARetry() = runTest {
        given()
        val failing = newViewModel(pdf = RecordingPdfRenderer(failure = IOException("no space")))
        enter(subject = failing)

        failing.onAction(ExportPreviewAction.Export)

        assertThat(failing.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORT_FAILED)
        assertThat(failing.uiState.value.navigation).isNull()
        assertThat(paymentGateway.entitlement().freeCredits).isEqualTo(1)
        assertThat(exportHistory.observeExports(APPLICATION_ID).first()).isEmpty()
    }

    @Test
    fun retryAfterAnExportFailure_exportsAgain() = runTest {
        given()
        var failures = 1
        val flaky = object : ResumePdfRenderer {
            override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume {
                if (failures-- > 0) throw IOException("no space")
                return RenderedResume(file = File(fileName), pageCount = 1)
            }
        }
        val subject = newViewModel(pdf = flaky)
        enter(subject = subject)
        subject.onAction(ExportPreviewAction.Export)
        assertThat(subject.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORT_FAILED)

        subject.onAction(ExportPreviewAction.RetryPreview)

        assertThat(subject.uiState.value.navigation).isInstanceOf(ExportPreviewNavigation.Exported::class.java)
    }

    @Test
    fun offlineScenario_keepsThePreviewReadableAndBlocksTheDownload() = runTest {
        given()
        enter(scenario = DebugScenario.OFFLINE)

        val state = viewModel.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.sheet).isNotNull()
        assertThat(state.canExport).isFalse()

        viewModel.onAction(ExportPreviewAction.Export)

        assertThat(pdfRenderer.callCount).isEqualTo(0)
    }

    @Test
    fun theConnectivityMonitorDecidesOffline() = runTest {
        given()
        connectivity.setOnline(false)
        enter()
        assertThat(viewModel.uiState.value.isOffline).isTrue()

        connectivity.setOnline(true)

        assertThat(viewModel.uiState.value.isOffline).isFalse()
    }

    @Test
    fun noDocument_whenTheProfileHasNoEntries() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalProfileWithoutEntries)
        enter()

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
        assertThat(viewModel.uiState.value.canExport).isFalse()
    }

    @Test
    fun noDocument_whenThereIsNoTailoredResume() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(tailoredResume = null)))
        profileRepository.sendProfile(canonicalCandidateProfile)
        enter()

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.NO_DOCUMENT)
    }

    @Test
    fun missingApplication_isAPreviewFailure() = runTest {
        applicationRepository.sendApplications(emptyList())
        enter()

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_FAILED)
    }

    @Test
    fun retryAfterAPreviewFailure_rendersThePreviewAgain() = runTest {
        given()
        enter(scenario = DebugScenario.ERROR)
        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_FAILED)

        viewModel.onAction(ExportPreviewAction.RetryPreview)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(viewModel.uiState.value.sheet).isNotNull()
    }

    @Test
    fun exportingScenario_exportsAsSoonAsThePreviewIsReady() = runTest {
        given()
        enter(scenario = DebugScenario.EXPORTING)

        assertThat(pdfRenderer.callCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.navigation).isInstanceOf(ExportPreviewNavigation.Exported::class.java)
    }

    @Test
    fun everyScenarioNeverShowsAPreviewWithoutADocument() = runTest {
        applicationRepository.sendApplications(emptyList())
        for (scenario in DebugScenario.entries) {
            val fresh = newViewModel()
            enter(scenario = scenario, subject = fresh)

            assertThat(fresh.uiState.value.sheet).isNull()
            assertThat(fresh.uiState.value.canExport).isFalse()
        }
    }

    @Test
    fun previewCarriesNoScoreOrGuaranteeClaim() = runTest {
        given()
        enter()

        val sheet = requireNotNull(viewModel.uiState.value.sheet)
        val text = buildString {
            append(sheet.name, sheet.contactLine, sheet.headline, sheet.skills.joinToString())
            sheet.sections.forEach { section ->
                append(section.heading)
                section.entries.forEach { entry ->
                    append(entry.title, entry.organization, entry.dateRange, entry.bullets.joinToString())
                }
            }
        }.lowercase()
        assertThat(text).doesNotContain("guarantee")
        assertThat(text).doesNotContain("ats score")
    }
}
