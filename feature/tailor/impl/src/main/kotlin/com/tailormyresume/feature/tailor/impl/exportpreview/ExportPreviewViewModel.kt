package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.CreditSpend
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.feature.tailor.api.navigation.ExportPreviewNavKey
import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.export.RenderedResume
import com.tailormyresume.feature.tailor.impl.export.ResumePdfRenderer
import com.tailormyresume.feature.tailor.impl.export.docx.ResumeDocxRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
internal class ExportPreviewViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val assembler: ResumeDocumentAssembler,
    private val pdfRenderer: ResumePdfRenderer,
    private val docxRenderer: ResumeDocxRenderer,
    private val paymentGateway: PaymentGateway,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val connectivityMonitor: ConnectivityMonitor,
    private val pendingExportStart: PendingExportStart,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ExportPreviewUiState())

    private var hasEntered = false

    private var applicationId: String = ""

    private var scenario: DebugScenario = DebugScenario.defaultValue

    private var document: ResumeDocument? = null

    private var exportJob: Job? = null

    private var canCancelExport = false

    val uiState: StateFlow<ExportPreviewUiState> = mutableState.asStateFlow()

    fun onEnter(key: ExportPreviewNavKey) {
        if (hasEntered) return
        hasEntered = true
        applicationId = key.applicationId
        scenario = key.scenario
        mutableState.value = ExportPreviewUiState(
            stage = exportPreviewStageFor(key.scenario),
            format = exportFormatFromWire(key.format),
            isOffline = exportPreviewIsOffline(key.scenario),
        )
        if (exportPreviewIsStatic(key.scenario)) return
        observeCredits()
        observeConnectivity()
        observePendingExportStart()
        viewModelScope.launch {
            loadDocument()
            if (scenario == DebugScenario.EXPORTING) export()
        }
    }

    fun onAction(action: ExportPreviewAction) {
        when (action) {
            is ExportPreviewAction.SelectFormat -> onSelectFormat(action.format)
            ExportPreviewAction.Export -> export()
            ExportPreviewAction.RetryPreview -> onRetry()
            ExportPreviewAction.NavigationHandled -> mutableState.update { state -> state.copy(navigation = null) }
            ExportPreviewAction.CancelExport -> onCancelExport()
        }
    }

    private fun observePendingExportStart() {
        viewModelScope.launch {
            pendingExportStart.applicationId.collect { requested ->
                if (requested == applicationId && pendingExportStart.consume(requested)) export()
            }
        }
    }

    private fun observeCredits() {
        viewModelScope.launch {
            paymentGateway.observeEntitlement().collect { entitlement ->
                mutableState.update { state ->
                    state.copy(
                        creditsKnown = true,
                        freeCredits = entitlement.freeCredits,
                        purchasedCredits = entitlement.purchasedCredits,
                        alreadyUnlocked = applicationId in entitlement.unlockedApplicationIds,
                    )
                }
            }
        }
    }

    private fun observeConnectivity() {
        if (exportPreviewIsOffline(scenario)) return
        viewModelScope.launch {
            connectivityMonitor.isOnline.collect { online ->
                mutableState.update { state -> state.copy(isOffline = !online) }
            }
        }
    }

    private suspend fun loadDocument() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            document = null
            mutableState.update { state -> state.copy(stage = ExportPreviewStage.PREVIEW_FAILED, sheet = null) }
            return
        }
        val profile = profileRepository.observeProfile().first()
        val resume = application.tailoredResume
        val assembled = if (profile == null || resume == null) {
            null
        } else {
            assembler.assemble(profile = profile, resume = resume).takeUnless { assembled -> assembled.isEmpty }
        }
        if (assembled == null) {
            document = null
            mutableState.update { state ->
                state.copy(
                    stage = ExportPreviewStage.NO_DOCUMENT,
                    jobTitle = application.job.title,
                    jobCompany = application.job.company,
                    sheet = null,
                    fileName = "",
                )
            }
            return
        }
        document = assembled
        mutableState.update { state ->
            state.copy(
                stage = ExportPreviewStage.PREVIEW_READY,
                jobTitle = application.job.title,
                jobCompany = application.job.company,
                sheet = exportPreviewSheetOf(assembled),
                fileName = fileNameFor(
                    format = state.format,
                    document = assembled,
                    company = application.job.company,
                    role = application.job.title,
                ),
            )
        }
    }

    private fun fileNameFor(format: ExportFormat, document: ResumeDocument, company: String, role: String): String =
        ExportFileNames.build(
            format = format,
            name = document.name,
            company = company,
            role = role,
        )

    private fun onSelectFormat(format: ExportFormat) {
        val current = mutableState.value
        if (current.format == format || current.stage == ExportPreviewStage.EXPORTING) return
        val source = document
        mutableState.update { state ->
            state.copy(
                format = format,
                fileName = source?.let { assembled ->
                    fileNameFor(
                        format = format,
                        document = assembled,
                        company = state.jobCompany,
                        role = state.jobTitle,
                    )
                }.orEmpty(),
            )
        }
    }

    private fun export() {
        val state = mutableState.value
        val source = document
        if (source == null || !state.canExport) return
        if (state.needsCredits) {
            mutableState.update { current -> current.copy(navigation = ExportPreviewNavigation.BuyCredits) }
            return
        }
        val format = state.format
        val fileName = state.fileName
        mutableState.update { current -> current.copy(stage = ExportPreviewStage.EXPORTING) }
        exportJob = viewModelScope.launch {
            canCancelExport = true
            val rendered = try {
                when (format) {
                    ExportFormat.PDF -> pdfRenderer.render(document = source, fileName = fileName)
                    ExportFormat.DOCX -> RenderedResume(
                        file = docxRenderer.render(document = source, fileName = fileName),
                        pageCount = null,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                null
            }
            if (rendered != null) {
                finishExport(
                    format = format,
                    fileName = fileName,
                    pageCount = rendered.pageCount,
                )
            } else {
                markExportFailed()
            }
        }
    }

    private fun onCancelExport() {
        if (mutableState.value.stage != ExportPreviewStage.EXPORTING || !canCancelExport) return
        exportJob?.cancel()
        canCancelExport = false
        mutableState.update { state -> state.copy(stage = ExportPreviewStage.PREVIEW_READY) }
    }

    private suspend fun finishExport(
        format: ExportFormat,
        fileName: String,
        pageCount: Int?,
    ) {
        canCancelExport = false
        when (val spend = runCatching { paymentGateway.unlock(applicationId) }.getOrNull()) {
            is CreditSpend.Spent -> {
                exportHistoryRepository.record(
                    ExportRecord(
                        applicationId = applicationId,
                        format = format,
                        fileName = fileName,
                        exportedAt = clock.now(),
                        creditKind = spend.kind,
                        pageCount = pageCount,
                        templateName = TEMPLATE_NAME,
                    ),
                )
                mutableState.update { state ->
                    state.copy(
                        stage = ExportPreviewStage.PREVIEW_READY,
                        navigation = ExportPreviewNavigation.Exported(
                            format = format,
                            spentFreeCredit = spend.kind == CreditKind.FREE,
                        ),
                    )
                }
            }

            CreditSpend.NoCreditLeft -> mutableState.update { state ->
                state.copy(stage = ExportPreviewStage.PREVIEW_READY, navigation = ExportPreviewNavigation.BuyCredits)
            }

            null -> markExportFailed()
        }
    }

    private fun markExportFailed() {
        mutableState.update { state -> state.copy(stage = ExportPreviewStage.EXPORT_FAILED) }
    }

    private fun onRetry() {
        when (mutableState.value.stage) {
            ExportPreviewStage.EXPORT_FAILED -> {
                mutableState.update { state -> state.copy(stage = ExportPreviewStage.PREVIEW_READY) }
                export()
            }

            ExportPreviewStage.PREVIEW_FAILED -> {
                mutableState.update { state -> state.copy(stage = ExportPreviewStage.RENDERING, sheet = null) }
                viewModelScope.launch { loadDocument() }
            }

            else -> Unit
        }
    }
}

private const val TEMPLATE_NAME = "Plain"
