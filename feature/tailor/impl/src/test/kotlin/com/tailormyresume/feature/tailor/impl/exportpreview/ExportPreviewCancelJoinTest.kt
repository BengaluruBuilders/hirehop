package com.tailormyresume.feature.tailor.impl.exportpreview

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import java.io.File

private const val JOIN_APPLICATION_ID = "application-northwind-1"

private class SlowToStopPdfRenderer(private val cleanupGate: CompletableDeferred<Unit>) : ResumePdfRenderer {
    var finishedStopping = false
        private set

    override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume {
        try {
            CompletableDeferred<Unit>().await()
        } catch (cancellation: CancellationException) {
            withContext(NonCancellable) { cleanupGate.await() }
            finishedStopping = true
            throw cancellation
        }
        return RenderedResume(file = File(fileName), pageCount = 1)
    }
}

private class JoinProbeDocxRenderer(private val pdf: SlowToStopPdfRenderer) : ResumeDocxRenderer {
    var startedAfterThePdfStopped: Boolean? = null
        private set

    override suspend fun render(document: ResumeDocument, fileName: String): File {
        startedAfterThePdfStopped = pdf.finishedStopping
        return File(fileName)
    }
}

class ExportPreviewCancelJoinTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun aNewExportWaitsForTheCancelledRenderToFinish() = runTest {
        val cleanupGate = CompletableDeferred<Unit>()
        val pdf = SlowToStopPdfRenderer(cleanupGate)
        val docx = JoinProbeDocxRenderer(pdf)
        val applications = TestApplicationRepository().apply { sendApplications(listOf(canonicalApplication)) }
        val profiles = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
        val viewModel = ExportPreviewViewModel(
            applicationRepository = applications,
            profileRepository = profiles,
            assembler = ResumeDocumentAssembler(TestResumeHeadings),
            pdfRenderer = pdf,
            docxRenderer = docx,
            paymentGateway = TestPaymentGateway(),
            exportHistoryRepository = TestExportHistoryRepository(),
            connectivityMonitor = TestConnectivityMonitor(),
            pendingExportStart = PendingExportStart(),
            clock = TestClock(),
        )
        viewModel.onEnter(ExportPreviewNavKey(JOIN_APPLICATION_ID, "pdf", DebugScenario.DEFAULT))

        viewModel.onAction(ExportPreviewAction.Export)
        viewModel.onAction(ExportPreviewAction.CancelExport)
        viewModel.onAction(ExportPreviewAction.SelectFormat(ExportFormat.DOCX))
        viewModel.onAction(ExportPreviewAction.Export)
        cleanupGate.complete(Unit)

        assertThat(docx.startedAfterThePdfStopped).isTrue()
    }
}
