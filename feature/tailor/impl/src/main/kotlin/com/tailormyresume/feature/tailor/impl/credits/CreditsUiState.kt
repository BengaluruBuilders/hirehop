package com.tailormyresume.feature.tailor.impl.credits

import com.tailormyresume.core.model.DebugScenario

internal enum class CreditsStage {
    LOADING,
    READY,
    ERROR,
}

internal data class CreditsPurchaseEntry(
    val orderId: String,
    val credits: Int,
    val formattedPrice: String,
    val formattedDate: String,
    val isPending: Boolean,
)

internal data class CreditsUiState(
    val stage: CreditsStage = CreditsStage.LOADING,
    val freeCredits: Int = 0,
    val purchasedCredits: Int = 0,
    val purchases: List<CreditsPurchaseEntry> = emptyList(),
    val hasPurchaseHistory: Boolean = false,
    val isOffline: Boolean = false,
    val creditsNeverExpire: Boolean = true,
) {
    val totalCredits: Int get() = freeCredits + purchasedCredits

    val showsFreeNote: Boolean get() = freeCredits > 0 && purchases.isEmpty()

    val refundOrderId: String? get() = purchases.firstOrNull { entry -> !entry.isPending }?.orderId
        ?: purchases.firstOrNull()?.orderId

    val canBuy: Boolean get() = stage == CreditsStage.READY && !isOffline
}

internal fun creditsIsStatic(scenario: DebugScenario): Boolean = scenario == DebugScenario.LOADING

internal fun creditsIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

internal fun creditsFailsToLoad(scenario: DebugScenario): Boolean = scenario == DebugScenario.ERROR
