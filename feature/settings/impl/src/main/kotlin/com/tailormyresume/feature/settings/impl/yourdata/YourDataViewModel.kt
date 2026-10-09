package com.tailormyresume.feature.settings.impl.yourdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseRecord
import com.tailormyresume.core.domain.PurchaseState
import com.tailormyresume.core.domain.account.DeleteMyDataUseCase
import com.tailormyresume.core.domain.account.ExportAccountDataUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.ProfileFactCounts
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.feature.settings.api.navigation.YourDataNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YourDataViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    paymentGateway: PaymentGateway,
    connectivityMonitor: ConnectivityMonitor,
    private val applicationRepository: ApplicationRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val exportAccountData: ExportAccountDataUseCase,
    private val deleteMyData: DeleteMyDataUseCase,
) : ViewModel() {

    private val local = MutableStateFlow(LocalState())

    private val shareEvents = Channel<YourDataEvent>(Channel.BUFFERED)

    val events: Flow<YourDataEvent> = shareEvents.receiveAsFlow()

    private val ledger: Flow<Ledger> = combine(
        profileRepository.observeProfile(),
        applicationRepository.observeApplications(),
        paymentGateway.observePurchaseHistory(),
        flow { emit(runCatching { paymentGateway.packs() }.getOrDefault(emptyList())) },
        ::Ledger,
    )

    val uiState: StateFlow<YourDataUiState> = combine(
        ledger,
        connectivityMonitor.isOnline,
        local,
    ) { ledger, isOnline, localState ->
        val applications = ledger.applications.map { application -> application.toListItem() }
        val counts = ledger.profile?.factCounts() ?: ProfileFactCounts(total = 0, confirmed = 0, userStated = 0)
        YourDataUiState.Content(
            profileFactCount = counts.total,
            confirmedFactCount = counts.confirmed,
            userStatedFactCount = counts.userStated,
            applications = applications,
            purchases = ledger.purchases.map { record -> record.toPurchase(ledger.packs) },
            isOffline = !isOnline || localState.forcedOffline,
            export = localState.export,
            deleteTarget = applications.firstOrNull { item -> item.id == localState.deleteTargetId },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = YourDataUiState.Loading,
    )

    fun onEnter(key: YourDataNavKey) {
        local.update { state ->
            state.copy(
                forcedOffline = key.scenario == DebugScenario.OFFLINE,
                export = if (key.scenario == DebugScenario.EXPORTING) YourDataExport.PREPARING else state.export,
            )
        }
    }

    fun onDownload() {
        val content = uiState.value as? YourDataUiState.Content ?: return
        if (content.isOffline || content.export == YourDataExport.PREPARING) return
        local.update { state -> state.copy(export = YourDataExport.PREPARING) }
        viewModelScope.launch {
            try {
                val archive = exportAccountData()
                shareEvents.send(YourDataEvent.ShareArchive(archive.file))
                local.update { state -> state.copy(export = YourDataExport.IDLE) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                local.update { state -> state.copy(export = YourDataExport.FAILED) }
            }
        }
    }

    fun onDeleteRequested(applicationId: String) {
        val content = uiState.value as? YourDataUiState.Content ?: return
        if (content.isOffline || content.applications.none { item -> item.id == applicationId }) return
        local.update { state -> state.copy(deleteTargetId = applicationId) }
    }

    fun onDeleteDismissed() {
        local.update { state -> state.copy(deleteTargetId = null) }
    }

    fun onDeleteConfirmed() {
        val target = (uiState.value as? YourDataUiState.Content)?.deleteTarget ?: return
        local.update { state -> state.copy(deleteTargetId = null) }
        viewModelScope.launch {
            applicationRepository.deleteApplication(target.id)
            exportHistoryRepository.clearFor(target.id)
        }
    }

    fun onDeleteMyDataRequested() = Unit

    fun onDeleteMyDataDismissed() = Unit

    fun onDeleteMyDataConfirmed() = Unit

    private class Ledger(
        val profile: CandidateProfile?,
        val applications: List<JobApplication>,
        val purchases: List<PurchaseRecord>,
        val packs: List<ApplicationPack>,
    )

    private data class LocalState(
        val forcedOffline: Boolean = false,
        val export: YourDataExport = YourDataExport.IDLE,
        val deleteTargetId: String? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun JobApplication.toListItem(): YourDataApplication =
    YourDataApplication(id = id, title = job.title, company = job.company)

private fun PurchaseRecord.toPurchase(packs: List<ApplicationPack>): YourDataPurchase {
    val pack = packs.firstOrNull { candidate -> candidate.id == packId }
    return YourDataPurchase(
        credits = pack?.credits,
        priceInPaise = pack?.priceInPaise,
        currencyCode = pack?.currencyCode,
        purchasedAt = purchasedAt,
        isPending = state == PurchaseState.PENDING,
    )
}
