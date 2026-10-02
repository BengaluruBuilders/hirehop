package com.hirehop.feature.tailor.impl.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseRecord
import com.hirehop.core.domain.PurchaseState
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CreditsViewModel @Inject constructor(
    private val paymentGateway: PaymentGateway,
    private val connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val mutableState = MutableStateFlow(CreditsUiState())

    private var hasEntered = false

    private var scenario: DebugScenario = DebugScenario.defaultValue

    private var loadJob: Job? = null

    val uiState: StateFlow<CreditsUiState> = mutableState.asStateFlow()

    fun onEnter(key: CreditsNavKey) {
        if (hasEntered) return
        hasEntered = true
        scenario = key.scenario
        mutableState.value = CreditsUiState(isOffline = creditsIsOffline(key.scenario))
        if (creditsIsStatic(key.scenario)) return
        if (creditsFailsToLoad(key.scenario)) {
            mutableState.value = mutableState.value.copy(stage = CreditsStage.ERROR)
            return
        }
        load()
    }

    fun onAction(action: CreditsAction) {
        when (action) {
            CreditsAction.Retry -> {
                mutableState.value = CreditsUiState(isOffline = creditsIsOffline(scenario))
                load()
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val packs = runCatching { paymentGateway.packs() }.getOrNull()
            if (packs == null) {
                mutableState.value = mutableState.value.copy(stage = CreditsStage.ERROR)
                return@launch
            }
            val online: Flow<Boolean> = if (creditsIsOffline(scenario)) flowOf(false) else connectivityMonitor.isOnline
            combine(
                paymentGateway.observeEntitlement(),
                paymentGateway.observePurchaseHistory(),
                online,
            ) { entitlement, history, isOnline ->
                CreditsUiState(
                    stage = CreditsStage.READY,
                    freeCredits = entitlement.freeCredits,
                    purchasedCredits = entitlement.purchasedCredits,
                    purchases = history.mapNotNull { record -> entryFor(record = record, packs = packs) },
                    isOffline = !isOnline,
                    creditsNeverExpire = packs.none { pack -> pack.creditsExpire },
                )
            }.collect { state -> mutableState.value = state }
        }
    }

    private fun entryFor(record: PurchaseRecord, packs: List<ApplicationPack>): CreditsPurchaseEntry? {
        val pack = packs.firstOrNull { candidate -> candidate.id == record.packId } ?: return null
        return CreditsPurchaseEntry(
            orderId = record.orderId,
            credits = pack.credits,
            formattedPrice = pack.formattedPrice(),
            formattedDate = record.purchasedAt.formattedDate(),
            isPending = record.state == PurchaseState.PENDING,
        )
    }
}
