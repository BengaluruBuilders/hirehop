package com.hirehop.feature.tailor.impl.exportpreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.impl.credits.formattedPrice
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.export.ResumePdfRenderer
import com.hirehop.feature.tailor.impl.export.docx.ResumeDocxRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExportPreviewViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val assembler: ResumeDocumentAssembler,
    private val pdfRenderer: ResumePdfRenderer,
    private val docxRenderer: ResumeDocxRenderer,
    private val paymentGateway: PaymentGateway,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ExportPreviewUiState())

    private var hasEntered = false

    private var applicationId: String = ""

    private var scenario: DebugScenario = DebugScenario.defaultValue

    private var document: ResumeDocument? = null

    val uiState: StateFlow<ExportPreviewUiState> = mutableState.asStateFlow()

    fun onEnter(key: ExportPreviewNavKey) {
        if (hasEntered) return
        hasEntered = true
        applicationId = key.applicationId
        scenario = key.scenario
        val format = ExportFormat.fromWire(key.format)
        mutableState.value = ExportPreviewUiState(
            stage = exportPreviewStageFor(key.scenario),
            format = format,
            isOffline = exportPreviewIsOffline(key.scenario),
        )
        if (exportPreviewIsStatic(key.scenario)) return
        viewModelScope.launch {
            loadDocument()
            loadCredits()
            if (exportPreviewExportsOnEntry(key.scenario)) export()
        }
    }

    fun onAction(action: ExportPreviewAction) {
        when (action) {
            is ExportPreviewAction.SelectFormat -> onSelectFormat(action.format)
            ExportPreviewAction.Export -> export()
            ExportPreviewAction.RetryPreview -> onRetryPreview()
            ExportPreviewAction.DismissResult -> onDismissResult()
            ExportPreviewAction.NavigateBack -> Unit
            is ExportPreviewAction.OpenExported -> Unit
        }
    }

    private suspend fun loadDocument() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            document = null
            mutableState.value = mutableState.value.copy(
                stage = ExportPreviewStage.PREVIEW_FAILED,
                sheet = null,
            )
            return
        }
        val profile = profileRepository.observeProfile().first()
        val resume = application.tailoredResume
        if (profile == null || resume == null) {
            document = null
            mutableState.value = mutableState.value.copy(
                stage = ExportPreviewStage.NO_DOCUMENT,
                jobTitle = application.job.title,
                jobCompany = application.job.company,
                sheet = null,
                fileName = "",
            )
            return
        }
        val assembled = assembler.assemble(profile = profile, resume = resume)
        if (assembled.isEmpty) {
            document = null
            mutableState.value = mutableState.value.copy(
                stage = ExportPreviewStage.NO_DOCUMENT,
                jobTitle = application.job.title,
                jobCompany = application.job.company,
                sheet = null,
                fileName = "",
            )
            return
        }
        document = assembled
        mutableState.value = mutableState.value.copy(
            stage = if (exportPreviewIsOffline(scenario)) {
                ExportPreviewStage.OFFLINE
            } else {
                ExportPreviewStage.PREVIEW_READY
            },
            jobTitle = application.job.title,
            jobCompany = application.job.company,
            sheet = exportPreviewSheetOf(assembled),
            fileName = ExportFileNames.build(
                format = mutableState.value.format,
                name = assembled.name,
                company = application.job.company,
                role = application.job.title,
            ),
        )
    }

    private suspend fun loadCredits() {
        val entitlement = runCatching { paymentGateway.entitlement() }.getOrNull() ?: return
        val packs = runCatching { paymentGateway.packs() }.getOrNull().orEmpty()
        val fromFree = entitlement.freeCredits > 0
        val left = if (fromFree) entitlement.freeCredits else entitlement.purchasedCredits
        mutableState.update { state ->
            state.copy(
                creditsLeft = left,
                isFreeCredit = fromFree,
                creditKnown = true,
                packPrice = packs.firstOrNull()?.formattedPrice().orEmpty(),
                stage = if (left == 0 && state.stage == ExportPreviewStage.PREVIEW_READY) {
                    ExportPreviewStage.NO_CREDIT
                } else {
                    state.stage
                },
            )
        }
    }

    private fun fileNameFor(state: ExportPreviewUiState, document: ResumeDocument): String = ExportFileNames.build(
        format = state.format,
        name = document.name,
        company = state.jobCompany,
        role = state.jobTitle,
    )

    private fun onSelectFormat(format: ExportFormat) {
        val current = mutableState.value
        if (current.format == format) return
        if (current.stage == ExportPreviewStage.EXPORTING) return
        val ready = current.stage != ExportPreviewStage.PREVIEW_READY
        val fileName = document
            ?.let { assembled -> fileNameFor(state = current.copy(format = format), document = assembled) }
            .orEmpty()
        mutableState.update { state ->
            state.copy(
                format = format,
                fileName = fileName,
                stage = if (ready) state.stage else ExportPreviewStage.PREVIEW_READY,
                exportedFormat = null,
            )
        }
    }

    private fun export() {
        val state = mutableState.value
        val source = document
        if (source == null || !state.canExport) return
        mutableState.value = state.copy(stage = ExportPreviewStage.EXPORTING)
        viewModelScope.launch {
            val format = mutableState.value.format
            val fileName = mutableState.value.fileName
            val written = try {
                when (format) {
                    ExportFormat.PDF -> pdfRenderer.render(document = source, fileName = fileName)
                    ExportFormat.DOCX -> docxRenderer.render(document = source, fileName = fileName)
                }
                true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                false
            }
            if (written) spendCredit()
            mutableState.value = mutableState.value.copy(
                stage = if (written) ExportPreviewStage.EXPORT_SUCCEEDED else ExportPreviewStage.EXPORT_FAILED,
                exportedFormat = if (written) format else null,
            )
        }
    }

    private suspend fun spendCredit() {
        runCatching { paymentGateway.consumeCredit() }
    }

    private fun onRetryPreview() {
        mutableState.value = mutableState.value.copy(
            stage = ExportPreviewStage.RENDERING,
            sheet = null,
            fileName = "",
        )
        viewModelScope.launch { loadDocument() }
    }

    private fun onDismissResult() {
        mutableState.value = mutableState.value.copy(
            stage = if (document == null) ExportPreviewStage.NO_DOCUMENT else ExportPreviewStage.PREVIEW_READY,
            exportedFormat = null,
        )
    }
}
