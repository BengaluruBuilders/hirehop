package com.hirehop.feature.tailor.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.UpdateBulletDecisionUseCase
import com.hirehop.core.model.BulletDecision
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.export.ResumePdfRenderer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel(assistedFactory = TailorViewModel.Factory::class)
internal class TailorViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val updateBulletDecision: UpdateBulletDecisionUseCase,
    private val assembler: ResumeDocumentAssembler,
    private val pdfRenderer: ResumePdfRenderer,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val decisionMutex = Mutex()

    val uiState: StateFlow<TailorUiState> = combine(
        applicationRepository.observeApplication(applicationId),
        profileRepository.observeProfile(),
    ) { application, profile ->
        buildTailorUiState(application, profile, assembler)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TailorUiState.Loading,
    )

    private val mutableExportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = mutableExportState

    fun onAccept(bulletId: String) = setDecision(bulletId, BulletDecision.ACCEPTED)

    fun onReject(bulletId: String) = setDecision(bulletId, BulletDecision.REJECTED)

    fun onAcceptAllSafeChanges() {
        viewModelScope.launch {
            decisionMutex.withLock {
                latestSafeBulletIds().forEach {
                    updateBulletDecision(applicationId, it, BulletDecision.ACCEPTED)
                }
            }
        }
    }

    fun onExport() {
        val success = uiState.value as? TailorUiState.Success ?: return
        if (!success.canExport || mutableExportState.value == ExportUiState.Exporting) return
        mutableExportState.value = ExportUiState.Exporting
        viewModelScope.launch {
            mutableExportState.value = try {
                ExportUiState.Ready(pdfRenderer.render(success.document, success.exportFileName))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                ExportUiState.Failed
            }
        }
    }

    fun onExportHandled() {
        mutableExportState.value = ExportUiState.Idle
    }

    private fun setDecision(bulletId: String, decision: BulletDecision) {
        viewModelScope.launch {
            decisionMutex.withLock { updateBulletDecision(applicationId, bulletId, decision) }
        }
    }

    private suspend fun latestSafeBulletIds(): List<String> {
        val application = applicationRepository.observeApplication(applicationId).first()
        val profile = profileRepository.observeProfile().first()
        val state = buildTailorUiState(application, profile, assembler)
        return (state as? TailorUiState.Success)?.safeChangeBulletIds.orEmpty()
    }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): TailorViewModel
    }
}
