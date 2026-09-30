package com.hirehop.feature.tailor.impl.exported

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.TailoredResume
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.exportpreview.ExportFileNames
import com.hirehop.feature.tailor.impl.exportpreview.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExportedViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val assembler: ResumeDocumentAssembler,
    private val paymentGateway: PaymentGateway,
    private val fileStore: ExportedFileStore,
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
            format = ExportFormat.fromWire(key.format),
            creditSource = if (key.spentFreeCredit) {
                ExportedCreditSource.FREE
            } else {
                ExportedCreditSource.PAID
            },
        )
        if (exportedIsStatic(key.scenario)) return
        if (exportedHasNoApplication(key.scenario)) {
            mutableState.value = mutableState.value.copy(stage = ExportedStage.NO_APPLICATION)
            return
        }
        viewModelScope.launch { load() }
    }

    fun onAction(action: ExportedAction) {
        when (action) {
            ExportedAction.OpenStatusSheet -> onOpenStatusSheet()
            ExportedAction.DismissStatusSheet -> onDismissStatusSheet()
            is ExportedAction.ConfirmStatus -> onConfirmStatus(action.status)
            ExportedAction.RequestShare -> onRequestShare()
            ExportedAction.ShareHandedToSystem -> mutableState.update { it.copy(shareRequest = null) }
            ExportedAction.NavigateBack -> Unit
        }
    }

    private suspend fun load() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            mutableState.value = mutableState.value.copy(stage = ExportedStage.NO_APPLICATION)
            return
        }
        val assembled = assemble(application.tailoredResume) ?: run {
            mutableState.value = mutableState.value.copy(
                stage = ExportedStage.NO_FILE,
                jobTitle = application.job.title,
                jobCompany = application.job.company,
            )
            return
        }
        val fileName = fileNameFor(state = mutableState.value, document = assembled)
        mutableState.value = mutableState.value.copy(
            stage = ExportedStage.READY,
            jobTitle = application.job.title,
            jobCompany = application.job.company,
            fileName = fileName,
            fileOnDevice = fileStore.fileFor(fileName) != null,
            status = application.status,
        )
        loadCredits()
    }

    private suspend fun assemble(resume: TailoredResume?): ResumeDocument? {
        val profile = profileRepository.observeProfile().first() ?: return null
        if (resume == null) return null
        val assembled = assembler.assemble(profile = profile, resume = resume)
        return assembled.takeUnless { document -> document.isEmpty }
    }

    private suspend fun loadCredits() {
        val entitlement = runCatching { paymentGateway.entitlement() }.getOrNull() ?: return
        val packs = runCatching { paymentGateway.packs() }.getOrNull().orEmpty()
        mutableState.value = creditsState(entitlement = entitlement, packs = packs)
    }

    private fun creditsState(entitlement: PurchaseEntitlement, packs: List<ApplicationPack>): ExportedUiState {
        val fromFree = mutableState.value.creditSource == ExportedCreditSource.FREE
        val left = if (fromFree) entitlement.freeCredits else entitlement.purchasedCredits
        return mutableState.value.copy(
            creditsKnown = true,
            creditsBefore = left + 1,
            creditsLeft = left,
            creditsNeverExpire = packs.isNotEmpty() && packs.all { pack -> !pack.creditsExpire },
        )
    }

    private fun fileNameFor(state: ExportedUiState, document: ResumeDocument): String =
        ExportFileNames.build(
            format = state.format,
            name = document.name,
            company = state.jobCompany,
            role = state.jobTitle,
        )

    private fun refreshExportedFile() {
        if (mutableState.value.stage != ExportedStage.READY) return
        val present = fileStore.fileFor(mutableState.value.fileName) != null
        if (present == mutableState.value.fileOnDevice) return
        mutableState.update { state -> state.copy(fileOnDevice = present) }
    }

    private fun onOpenStatusSheet() {
        if (mutableState.value.stage != ExportedStage.READY) return
        mutableState.update { state -> state.copy(statusSheetOpen = true, statusJustSet = false) }
    }

    private fun onDismissStatusSheet() {
        mutableState.update { state -> state.copy(statusSheetOpen = false) }
    }

    private fun onConfirmStatus(status: ApplicationStatus) {
        val state = mutableState.value
        if (!state.statusSheetOpen) return
        mutableState.update { current ->
            current.copy(
                status = status,
                statusSheetOpen = false,
                statusJustSet = true,
            )
        }
        val id = applicationId
        viewModelScope.launch { applicationRepository.updateStatus(id = id, status = status) }
    }

    private fun onRequestShare() {
        val state = mutableState.value
        if (state.stage != ExportedStage.READY) return
        val file = fileStore.fileFor(state.fileName) ?: return
        mutableState.update { current ->
            current.copy(
                shareState = ExportedShareState.REQUESTED,
                statusJustSet = false,
                shareRequest = ExportedShareRequest(
                    file = file,
                    format = current.format,
                    jobTitle = current.jobTitle,
                    jobCompany = current.jobCompany,
                ),
            )
        }
    }
}
