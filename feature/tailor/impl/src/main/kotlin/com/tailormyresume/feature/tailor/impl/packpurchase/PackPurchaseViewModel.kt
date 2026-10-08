package com.tailormyresume.feature.tailor.impl.packpurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.domain.PurchaseResult
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.tailor.api.navigation.PackPurchaseNavKey
import com.tailormyresume.feature.tailor.impl.credits.formattedDate
import com.tailormyresume.feature.tailor.impl.credits.formattedPrice
import com.tailormyresume.feature.tailor.impl.exportpreview.PendingExportStart
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
internal class PackPurchaseViewModel @Inject constructor(
    private val paymentGateway: PaymentGateway,
    private val applicationRepository: ApplicationRepository,
    private val connectivityMonitor: ConnectivityMonitor,
    private val pendingExportStart: PendingExportStart,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(PackPurchaseUiState())

    private var hasEntered = false

    private var scenario: DebugScenario = DebugScenario.defaultValue

    private var requestedPackId: String = ""

    private var applicationId: String = ""

    private var startExportOnReturn: Boolean = false

    val uiState: StateFlow<PackPurchaseUiState> = mutableState.asStateFlow()

    fun onEnter(key: PackPurchaseNavKey) {
        if (hasEntered) return
        hasEntered = true
        scenario = key.scenario
        requestedPackId = key.packId
        applicationId = key.applicationId
        startExportOnReturn = key.startExportOnReturn
        mutableState.value = PackPurchaseUiState(
            stage = PackPurchaseStage.LOADING,
            selectedPackId = key.packId,
            isOffline = packPurchaseIsOffline(key.scenario),
            hasApplication = key.applicationId.isNotBlank(),
            format = exportFormatFromWire(key.format),
        )
        if (packPurchaseIsStatic(key.scenario)) return
        observeCredits()
        observeConnectivity()
        viewModelScope.launch { load() }
    }

    fun onAction(action: PackPurchaseAction) {
        when (action) {
            is PackPurchaseAction.Buy -> onBuy(action.packId)
            PackPurchaseAction.RetryBuy -> mutableState.value.selectedPack?.let { pack -> onBuy(pack.id) }
            PackPurchaseAction.ReloadPacks -> onReload()
            PackPurchaseAction.ReturnAfterPurchase -> onReturnAfterPurchase()
        }
    }

    private fun observeCredits() {
        viewModelScope.launch {
            paymentGateway.observeEntitlement().collect { entitlement ->
                mutableState.update { state -> state.copy(totalCredits = entitlement.totalCredits) }
            }
        }
    }

    private fun observeConnectivity() {
        if (packPurchaseIsOffline(scenario)) return
        viewModelScope.launch {
            connectivityMonitor.isOnline.collect { online ->
                mutableState.update { state -> state.copy(isOffline = !online) }
            }
        }
    }

    private suspend fun load() {
        loadJob()
        val packs = if (packPurchaseHidesCatalogue(scenario)) null else runCatching { paymentGateway.packs() }.getOrNull()
        if (packs == null) {
            mutableState.update { state ->
                state.copy(stage = PackPurchaseStage.FAILED, packs = emptyList(), failureReason = null)
            }
            return
        }
        mutableState.update { state ->
            state.copy(
                stage = PackPurchaseStage.READY,
                packs = packs,
                selectedPackId = selectedPackIdFor(packs = packs),
                failureReason = null,
            )
        }
    }

    private suspend fun loadJob() {
        if (applicationId.isBlank()) return
        val application = applicationRepository.observeApplication(applicationId).first() ?: return
        mutableState.update { state ->
            state.copy(jobTitle = application.job.title, jobCompany = application.job.company)
        }
    }

    private fun onReturnAfterPurchase() {
        if (startExportOnReturn && applicationId.isNotBlank() && mutableState.value.stage == PackPurchaseStage.SUCCESS) {
            pendingExportStart.request(applicationId)
        }
    }

    private fun onReload() {
        mutableState.update { state -> state.copy(stage = PackPurchaseStage.LOADING, failureReason = null) }
        viewModelScope.launch { load() }
    }

    private fun onBuy(packId: String) {
        val state = mutableState.value
        val pack = state.packs.firstOrNull { candidate -> candidate.id == packId }
        val buyable = state.stage == PackPurchaseStage.READY || state.stage == PackPurchaseStage.CANCELLED ||
            state.stage == PackPurchaseStage.FAILED
        if (pack == null || state.isOffline || !buyable) return
        mutableState.update { current ->
            current.copy(
                stage = PackPurchaseStage.PURCHASING,
                selectedPackId = packId,
                creditsBefore = current.totalCredits,
                failureReason = null,
            )
        }
        viewModelScope.launch {
            val result = runCatching { paymentGateway.purchase(packId) }.getOrNull()
            mutableState.update { current -> stateFor(current = current, pack = pack, result = result) }
            if (result is PurchaseResult.Completed) recordReceipt(pack)
        }
    }

    private suspend fun recordReceipt(pack: ApplicationPack) {
        val record = runCatching { paymentGateway.purchaseHistory() }.getOrNull()
            ?.firstOrNull { entry -> entry.packId == pack.id }
        val receipt = PackPurchaseReceipt(
            credits = pack.credits,
            formattedPrice = pack.formattedPrice(),
            formattedDate = (record?.purchasedAt ?: clock.now()).formattedDate(),
        )
        mutableState.update { state -> state.copy(receipt = receipt) }
    }

    private fun stateFor(
        current: PackPurchaseUiState,
        pack: ApplicationPack,
        result: PurchaseResult?,
    ): PackPurchaseUiState = when (result) {
        is PurchaseResult.Completed -> current.copy(
            stage = PackPurchaseStage.SUCCESS,
            totalCredits = result.entitlement.totalCredits,
            receipt = PackPurchaseReceipt(
                credits = pack.credits,
                formattedPrice = pack.formattedPrice(),
                formattedDate = clock.now().formattedDate(),
            ),
        )

        is PurchaseResult.Pending -> current.copy(stage = PackPurchaseStage.PENDING)
        PurchaseResult.Cancelled -> current.copy(stage = PackPurchaseStage.CANCELLED)
        is PurchaseResult.Failed -> current.copy(stage = PackPurchaseStage.FAILED, failureReason = result.reason)
        null -> current.copy(
            stage = PackPurchaseStage.FAILED,
            failureReason = PurchaseFailureReason.PaymentUnavailable,
        )
    }

    private fun selectedPackIdFor(packs: List<ApplicationPack>): String = when {
        packs.any { pack -> pack.id == requestedPackId } -> requestedPackId
        else -> packs.firstOrNull()?.id.orEmpty()
    }
}
