package com.hirehop.feature.tailor.impl.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditsViewModel @Inject constructor(
    private val paymentGateway: PaymentGateway,
) : ViewModel() {

    private val mutableState = MutableStateFlow(CreditsUiState())

    private var hasEntered = false

    private var scenario: DebugScenario = DebugScenario.defaultValue

    val uiState: StateFlow<CreditsUiState> = mutableState.asStateFlow()

    fun onEnter(key: CreditsNavKey) {
        if (hasEntered) return
        hasEntered = true
        scenario = key.scenario
        mutableState.value = CreditsUiState(
            stage = creditsStageFor(key.scenario),
            isOffline = creditsIsOffline(key.scenario),
        )
        if (creditsIsStatic(key.scenario)) return
        viewModelScope.launch { load() }
    }

    fun onAction(action: CreditsAction) {
        when (action) {
            CreditsAction.Restore -> onRestore()
            CreditsAction.Dismiss -> onDismiss()
        }
    }

    private suspend fun load() {
        if (creditsFailsToLoad(scenario)) {
            mutableState.value = mutableState.value.copy(
                stage = CreditsStage.ERROR,
                failureReason = null,
                isOffline = creditsIsOffline(scenario),
            )
            return
        }
        val packs = runCatching { paymentGateway.packs() }.getOrNull().orEmpty()
        val entitlement = runCatching { paymentGateway.entitlement() }.getOrNull()
        if (entitlement == null) {
            mutableState.value = mutableState.value.copy(
                stage = CreditsStage.ERROR,
                failureReason = null,
                isOffline = creditsIsOffline(scenario),
            )
            return
        }
        val seeded = seededEntitlement(entitlement = entitlement, packs = packs, scenario = scenario)
        mutableState.value = stateFor(
            entitlement = seeded,
            packs = packs,
            stage = stageFor(scenario = scenario, entitlement = seeded),
            failureReason = null,
            isOffline = creditsIsOffline(scenario),
        )
    }

    private fun onRestore() {
        val state = mutableState.value
        if (state.stage == CreditsStage.RESTORING) return
        mutableState.value = state.copy(stage = CreditsStage.RESTORING, failureReason = null)
        viewModelScope.launch {
            val packs = runCatching { paymentGateway.packs() }.getOrNull().orEmpty()
            val restored = runCatching { paymentGateway.restorePurchases() }.getOrNull()
            mutableState.value = if (restored == null) {
                state.copy(
                    stage = CreditsStage.ERROR,
                    failureReason = PurchaseFailureReason.PurchaseUnavailable,
                )
            } else {
                stateFor(
                    entitlement = restored,
                    packs = packs,
                    stage = CreditsStage.RESTORED,
                    failureReason = null,
                    isOffline = state.isOffline,
                )
            }
        }
    }

    private fun onDismiss() {
        mutableState.update { state ->
            state.copy(
                stage = if (state.isOffline) CreditsStage.OFFLINE else CreditsStage.FREE_ONLY,
                failureReason = null,
            )
        }
    }

    private fun stateFor(
        entitlement: PurchaseEntitlement,
        packs: List<ApplicationPack>,
        stage: CreditsStage,
        failureReason: PurchaseFailureReason?,
        isOffline: Boolean,
    ): CreditsUiState = CreditsUiState(
        stage = stage,
        freeCredits = entitlement.freeCredits,
        purchasedCredits = entitlement.purchasedCredits,
        pendingPackIds = entitlement.pendingPackIds,
        purchases = entriesFor(entitlement = entitlement, packs = packs),
        failureReason = failureReason,
        isOffline = isOffline,
        purchasedCreditsNeverExpire = packs.isNotEmpty() && packs.all { pack -> !pack.creditsExpire },
        purchasedCreditsMayExpire = packs.any { pack -> pack.creditsExpire },
    )

    private fun entriesFor(
        entitlement: PurchaseEntitlement,
        packs: List<ApplicationPack>,
    ): List<CreditsPurchaseEntry> = packs
        .filter { pack -> pack.id in entitlement.pendingPackIds }
        .map { pack ->
            CreditsPurchaseEntry(
                packId = pack.id,
                packName = pack.name,
                credits = pack.credits,
                status = CreditsPurchaseStatus.PENDING,
                formattedPrice = pack.formattedPrice(),
                creditsExpire = pack.creditsExpire,
            )
        }

    private fun stageFor(scenario: DebugScenario, entitlement: PurchaseEntitlement): CreditsStage = when {
        creditsIsOffline(scenario) -> CreditsStage.OFFLINE
        creditsSeedsZero(scenario) -> CreditsStage.ZERO
        creditsSeedsPurchasedOnly(scenario) -> CreditsStage.PURCHASED_ONLY
        creditsSeedsMixed(scenario) -> CreditsStage.MIXED
        scenario == DebugScenario.DELETING -> CreditsStage.RESTORING
        else -> creditsStageFor(entitlement = entitlement, isPending = entitlement.pendingPackIds.isNotEmpty())
    }

    private fun seededEntitlement(
        entitlement: PurchaseEntitlement,
        packs: List<ApplicationPack>,
        scenario: DebugScenario,
    ): PurchaseEntitlement = when {
        creditsSeedsZero(scenario) -> PurchaseEntitlement(
            freeCredits = 0,
            purchasedCredits = 0,
            pendingPackIds = emptyList(),
        )
        creditsSeedsPurchasedOnly(scenario) -> entitlement.copy(
            purchasedCredits = creditsOf(packs = packs),
        )
        creditsSeedsMixed(scenario) -> entitlement.copy(
            freeCredits = 0,
            purchasedCredits = creditsOf(packs = packs),
        )
        else -> entitlement
    }

    private fun creditsOf(packs: List<ApplicationPack>): Int = packs.sumOf { pack -> pack.credits }
}
