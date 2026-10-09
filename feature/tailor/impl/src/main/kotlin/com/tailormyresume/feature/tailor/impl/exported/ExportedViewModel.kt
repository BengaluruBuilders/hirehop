package com.tailormyresume.feature.tailor.impl.exported

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.feature.tailor.api.navigation.ExportedNavKey
import com.tailormyresume.feature.tailor.impl.credits.formattedDate
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.exportpreview.ExportFileNames
import com.tailormyresume.feature.tailor.impl.exportpreview.exportFormatFromWire
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
internal class ExportedViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val assembler: ResumeDocumentAssembler,
    private val paymentGateway: PaymentGateway,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val fileStore: ExportedFileStore,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ExportedUiState())

    private var hasEntered = false

    private var applicationId: String = ""

    val uiState: StateFlow<ExportedUiState> = mutableState.asStateFlow()

    fun onEnter(key: ExportedNavKey) {
        if (hasEntered) {
            refreshExportedFile()
            return
        }
        hasEntered = true
        applicationId = key.applicationId
        mutableState.value = ExportedUiState(
            format = exportFormatFromWire(key.format),
            creditSource = if (key.spentFreeCredit) ExportedCreditSource.FREE else ExportedCreditSource.PAID,
        )
        if (exportedIsStatic(key.scenario)) return
        if (exportedHasNoApplication(key.scenario)) {
            mutableState.update { state -> state.copy(stage = ExportedStage.NO_APPLICATION) }
            return
        }
        observeCredits()
        viewModelScope.launch { load() }
    }

    fun onAction(action: ExportedAction) {
        when (action) {
            ExportedAction.OpenStatusSheet -> onOpenStatusSheet()
            ExportedAction.DismissStatusSheet -> mutableState.update { state -> state.copy(statusSheetOpen = false) }
            is ExportedAction.ConfirmStatus -> onConfirmStatus(action.status)
            ExportedAction.UndoStatus -> onUndoStatus()
            ExportedAction.DismissUndo -> mutableState.update { state -> state.copy(undoStatus = null) }
            ExportedAction.RequestShare -> onRequestFile(ExportedFileAction.SHARE)
            ExportedAction.RequestOpen -> onRequestFile(ExportedFileAction.OPEN)
            ExportedAction.FileRequestHandled -> mutableState.update { state -> state.copy(fileRequest = null) }
            ExportedAction.OpenUnavailable ->
                mutableState.update { state -> state.copy(fileRequest = null, openUnavailable = true) }
            ExportedAction.DismissOpenUnavailable -> mutableState.update { state -> state.copy(openUnavailable = false) }
        }
    }

    private fun observeCredits() {
        viewModelScope.launch {
            val neverExpire = runCatching { paymentGateway.packs() }.getOrNull()
                ?.let { packs -> packs.isNotEmpty() && packs.none { pack -> pack.creditsExpire } }
                ?: false
            paymentGateway.observeEntitlement().collect { entitlement ->
                mutableState.update { state ->
                    state.copy(
                        creditsKnown = true,
                        creditsLeft = entitlement.totalCredits,
                        creditsNeverExpire = neverExpire,
                    )
                }
            }
        }
    }

    private suspend fun load() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            mutableState.update { state -> state.copy(stage = ExportedStage.NO_APPLICATION) }
            return
        }
        val profile = profileRepository.observeProfile().first()
        val resume = application.tailoredResume
        val document = if (profile == null || resume == null) {
            null
        } else {
            assembler.assemble(profile = profile, resume = resume).takeUnless { assembled -> assembled.isEmpty }
        }
        if (document == null) {
            mutableState.update { state ->
                state.copy(
                    stage = ExportedStage.NO_FILE,
                    jobTitle = application.job.title,
                    jobCompany = application.job.company,
                )
            }
            return
        }
        val record = exportHistoryRepository.observeExports(applicationId).first().lastOrNull()
        mutableState.update { state ->
            val format = record?.format ?: state.format
            val fileName = record?.fileName ?: ExportFileNames.build(
                format = format,
                name = document.name,
                company = application.job.company,
                role = application.job.title,
            )
            state.copy(
                stage = ExportedStage.READY,
                format = format,
                jobTitle = application.job.title,
                jobCompany = application.job.company,
                fileName = fileName,
                fileOnDevice = fileStore.fileFor(fileName) != null,
                fileSizeBytes = fileStore.fileFor(fileName)?.length(),
                pageCount = record?.pageCount,
                templateName = record?.templateName,
                status = application.status,
            )
        }
    }

    private fun refreshExportedFile() {
        val state = mutableState.value
        if (state.stage != ExportedStage.READY) return
        val present = fileStore.fileFor(state.fileName) != null
        if (present != state.fileOnDevice) mutableState.update { current -> current.copy(fileOnDevice = present) }
    }

    private fun onOpenStatusSheet() {
        if (mutableState.value.stage != ExportedStage.READY) return
        mutableState.update { state -> state.copy(statusSheetOpen = true, undoStatus = null) }
    }

    private fun onConfirmStatus(status: ApplicationStatus) {
        val previous = mutableState.value.status
        if (!mutableState.value.statusSheetOpen) return
        mutableState.update { state ->
            state.copy(
                status = status,
                statusSheetOpen = false,
                markedOn = if (status == previous) state.markedOn else clock.now().formattedDate(),
                undoStatus = previous.takeIf { status != previous },
            )
        }
        persistStatus(status)
    }

    private fun onUndoStatus() {
        val restored = mutableState.value.undoStatus ?: return
        mutableState.update { state -> state.copy(status = restored, markedOn = null, undoStatus = null) }
        persistStatus(restored)
    }

    private fun persistStatus(status: ApplicationStatus) {
        val id = applicationId
        viewModelScope.launch { applicationRepository.updateStatus(id = id, status = status) }
    }

    private fun onRequestFile(action: ExportedFileAction) {
        val state = mutableState.value
        if (state.stage != ExportedStage.READY) return
        val file = fileStore.fileFor(state.fileName) ?: return
        mutableState.update { current ->
            current.copy(
                undoStatus = null,
                fileRequest = ExportedFileRequest(
                    file = file,
                    format = current.format,
                    action = action,
                    jobTitle = current.jobTitle,
                    jobCompany = current.jobCompany,
                ),
            )
        }
    }
}
