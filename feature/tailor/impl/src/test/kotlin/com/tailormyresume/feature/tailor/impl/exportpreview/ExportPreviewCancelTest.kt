package com.tailormyresume.feature.tailor.impl.exportpreview

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.CreditSpend
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.model.DebugScenario
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException

private const val CANCEL_APPLICATION_ID = "application-northwind-1"

private class SuspendingUnlockGateway(
    private val delegate: TestPaymentGateway,
    val unlockGate: CompletableDeferred<Unit>,
) : PaymentGateway by delegate {
    var unlockCalls = 0

    override suspend fun unlock(applicationId: String): CreditSpend {
        unlockCalls++
        unlockGate.await()
        return delegate.unlock(applicationId)
    }
}

private class InstantPdfRenderer : ResumePdfRenderer {
    override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume =
        RenderedResume(file = File(fileName), pageCount = 1)
}

private class FailsAfterCancelPdfRenderer(
    private val renderGate: CompletableDeferred<Unit>,
    private val failureGate: CompletableDeferred<Unit>,
) : ResumePdfRenderer {
    override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume {
        try {
            renderGate.await()
        } catch (cancellation: CancellationException) {
            withContext(NonCancellable) { failureGate.await() }
            throw IOException("render stopped")
        }
        return RenderedResume(file = File(fileName), pageCount = 1)
    }
}

private class NoDocx : ResumeDocxRenderer {
    override suspend fun render(document: ResumeDocument, fileName: String): File = File(fileName)
}

class ExportPreviewCancelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val exportHistory = TestExportHistoryRepository()
    private val testGateway = TestPaymentGateway()

    private fun newViewModel(pdf: ResumePdfRenderer, gateway: PaymentGateway = testGateway): ExportPreviewViewModel {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
        return ExportPreviewViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            assembler = ResumeDocumentAssembler(TestResumeHeadings),
            pdfRenderer = pdf,
            docxRenderer = NoDocx(),
            paymentGateway = gateway,
            exportHistoryRepository = exportHistory,
            connectivityMonitor = TestConnectivityMonitor(),
            pendingExportStart = PendingExportStart(),
            clock = TestClock(),
        ).also { it.onEnter(ExportPreviewNavKey(CANCEL_APPLICATION_ID, "pdf", DebugScenario.DEFAULT)) }
    }

    @Test
    fun cancelIsIgnoredOnceTheSpendHasStarted() = runTest {
        val unlockGate = CompletableDeferred<Unit>()
        val gateway = SuspendingUnlockGateway(testGateway, unlockGate)
        val viewModel = newViewModel(pdf = InstantPdfRenderer(), gateway = gateway)
        val freeBefore = testGateway.entitlement().freeCredits

        viewModel.onAction(ExportPreviewAction.Export)
        assertThat(gateway.unlockCalls).isEqualTo(1)
        viewModel.onAction(ExportPreviewAction.CancelExport)
        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.EXPORTING)
        unlockGate.complete(Unit)

        assertThat(viewModel.uiState.value.navigation).isInstanceOf(ExportPreviewNavigation.Exported::class.java)
        assertThat(exportHistory.observeExports(CANCEL_APPLICATION_ID).first()).hasSize(1)
        assertThat(testGateway.entitlement().freeCredits).isEqualTo(freeBefore - 1)
    }

    @Test
    fun spendingFlagIsOnWhileTheUnlockIsInFlightAndOffAfterwards() = runTest {
        val unlockGate = CompletableDeferred<Unit>()
        val viewModel = newViewModel(
            pdf = InstantPdfRenderer(),
            gateway = SuspendingUnlockGateway(testGateway, unlockGate),
        )

        assertThat(viewModel.uiState.value.isSpending).isFalse()
        viewModel.onAction(ExportPreviewAction.Export)
        assertThat(viewModel.uiState.value.isSpending).isTrue()
        unlockGate.complete(Unit)

        assertThat(viewModel.uiState.value.isSpending).isFalse()
    }

    @Test
    fun aCancelledRenderThatThenFailsDoesNotMarkTheExportFailed() = runTest {
        val failureGate = CompletableDeferred<Unit>()
        val viewModel = newViewModel(pdf = FailsAfterCancelPdfRenderer(CompletableDeferred(), failureGate))

        viewModel.onAction(ExportPreviewAction.Export)
        viewModel.onAction(ExportPreviewAction.CancelExport)
        failureGate.complete(Unit)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ExportPreviewStage.PREVIEW_READY)
        assertThat(exportHistory.observeExports(CANCEL_APPLICATION_ID).first()).isEmpty()
    }
}
