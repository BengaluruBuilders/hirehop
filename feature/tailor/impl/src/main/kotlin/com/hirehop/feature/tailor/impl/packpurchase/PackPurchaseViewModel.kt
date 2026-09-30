package com.hirehop.feature.tailor.impl.packpurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseResult
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.api.navigation.PackPurchaseNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PackPurchaseViewModel @Inject constructor(
    private val paymentGateway: PaymentGateway,
) : ViewModel() {

    private val mutableState = MutableStateFlow(PackPurchaseUiState())

    private var hasEntered = false

    private var scenario: DebugScenario = DebugScenario.defaultValue

    private var requestedPackId: String = ""

    val uiState: StateFlow<PackPurchaseUiState> = mutableState.asStateFlow()

    fun onEnter(key: PackPurchaseNavKey) {
        if (hasEntered) return
        hasEntered = true
        scenario = key.scenario
        requestedPackId = key.packId
        mutableState.value = PackPurchaseUiState(
            stage = packPurchaseStageFor(key.scenario),
            selectedPackId = key.packId,
            isOffline = packPurchaseIsOffline(key.scenario),
        )
        if (packPurchaseIsStatic(key.scenario)) return
        viewModelScope.launch { load() }
    }

    fun onAction(action: PackPurchaseAction) {
        when (action) {
            is PackPurchaseAction.SelectPack -> onSelectPack(action.packId)
            is PackPurchaseAction.Buy -> onBuy(action.packId)
            PackPurchaseAction.Restore -> onRestore()
            PackPurchaseAction.Dismiss -> onDismiss()
        }
    }

    private suspend fun load() {
        if (packPurchaseHidesCatalogue(scenario)) {
            mutableState.value = mutableState.value.copy(
                stage = PackPurchaseStage.FAILED,
                packs = emptyList(),
                entitlement = null,
                failureReason = null,
                isOffline = packPurchaseIsOffline(scenario),
            )
            return
        }
        val packs = runCatching { paymentGateway.packs() }.getOrNull()
        val entitlement = if (packs == null) null else runCatching { paymentGateway.entitlement() }.getOrNull()
        if (packs == null || entitlement == null) {
            mutableState.value = mutableState.value.copy(
                stage = PackPurchaseStage.FAILED,
                packs = emptyList(),
                entitlement = null,
                failureReason = null,
                isOffline = packPurchaseIsOffline(scenario),
            )
            return
        }
        val visiblePacks = if (packPurchaseShowsNoPacks(scenario)) emptyList() else packs
        val selectedId = selectedPackIdFor(packs = packs, requestedPackId = requestedPackId)
        mutableState.value = mutableState.value.copy(
            stage = packPurchaseStageFor(scenario = scenario),
            packs = visiblePacks,
            selectedPackId = selectedId,
            entitlement = seededEntitlement(
                entitlement = entitlement,
                packs = packs,
                selectedPackId = selectedId,
                scenario = scenario,
            ),
            failureReason = null,
            isOffline = packPurchaseIsOffline(scenario),
        )
    }

    private fun onSelectPack(packId: String) {
        mutableState.update { state ->
            val known = state.packs.any { pack -> pack.id == packId }
            if (known) state.copy(selectedPackId = packId) else state
        }
    }

    private fun onBuy(packId: String) {
        val state = mutableState.value
        if (state.isBuying) return
        mutableState.value = state.copy(
            stage = PackPurchaseStage.PURCHASING,
            selectedPackId = packId,
            failureReason = null,
        )
        viewModelScope.launch {
            val result = runCatching { paymentGateway.purchase(packId) }.getOrNull()
            mutableState.value = if (result == null) {
                state.copy(
                    stage = PackPurchaseStage.FAILED,
                    failureReason = PurchaseFailureReason.PaymentUnavailable,
                )
            } else {
                stateFor(result = result)
            }
        }
    }

    private fun onRestore() {
        val state = mutableState.value
        if (state.isRestoring) return
        mutableState.value = state.copy(stage = PackPurchaseStage.RESTORING, failureReason = null)
        viewModelScope.launch {
            val restored = runCatching { paymentGateway.restorePurchases() }.getOrNull()
            mutableState.value = if (restored == null) {
                state.copy(
                    stage = PackPurchaseStage.FAILED,
                    failureReason = PurchaseFailureReason.PurchaseUnavailable,
                )
            } else {
                state.copy(
                    stage = PackPurchaseStage.RESTORED,
                    entitlement = restored,
                    failureReason = null,
                )
            }
        }
    }

    private fun onDismiss() {
        mutableState.update { state ->
            state.copy(
                stage = if (state.isOffline) PackPurchaseStage.OFFLINE else PackPurchaseStage.READY,
                failureReason = null,
            )
        }
    }

    private fun stateFor(result: PurchaseResult): PackPurchaseUiState = when (result) {
        is PurchaseResult.Completed -> mutableState.value.copy(
            stage = PackPurchaseStage.SUCCESS,
            entitlement = result.entitlement,
            failureReason = null,
        )
        is PurchaseResult.Pending -> mutableState.value.copy(
            stage = PackPurchaseStage.PENDING,
            entitlement = result.entitlement,
            failureReason = null,
        )
        is PurchaseResult.Cancelled -> mutableState.value.copy(
            stage = PackPurchaseStage.CANCELLED,
            failureReason = null,
        )
        is PurchaseResult.Failed -> mutableState.value.copy(
            stage = PackPurchaseStage.FAILED,
            entitlement = result.entitlement,
            failureReason = result.reason,
        )
    }

    private fun seededEntitlement(
        entitlement: PurchaseEntitlement,
        packs: List<ApplicationPack>,
        selectedPackId: String,
        scenario: DebugScenario,
    ): PurchaseEntitlement {
        val purchased = entitlement.purchasedCredits + creditsOf(packs = packs, selectedPackId = selectedPackId)
        return when {
            packPurchaseSeedsRecordedPurchase(scenario) -> entitlement.copy(purchasedCredits = purchased)
            packPurchaseSeedsPartlyUsed(scenario) -> entitlement.copy(
                freeCredits = 0,
                purchasedCredits = purchased,
            )
            else -> entitlement
        }
    }

    private fun creditsOf(packs: List<ApplicationPack>, selectedPackId: String): Int {
        val pack = packs.firstOrNull { candidate -> candidate.id == selectedPackId }
            ?: packs.firstOrNull()
        return pack?.credits ?: 0
    }

    private fun selectedPackIdFor(packs: List<ApplicationPack>, requestedPackId: String): String = when {
        packs.any { pack -> pack.id == requestedPackId } -> requestedPackId
        else -> packs.firstOrNull()?.id.orEmpty()
    }
}
