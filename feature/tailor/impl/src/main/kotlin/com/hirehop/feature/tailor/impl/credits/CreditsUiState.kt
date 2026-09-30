package com.hirehop.feature.tailor.impl.credits

import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.model.DebugScenario
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class CreditsStage {
    IDLE,
    LOADING,
    FREE_ONLY,
    PURCHASED_ONLY,
    MIXED,
    ZERO,
    PENDING,
    RESTORING,
    RESTORED,
    ERROR,
    OFFLINE,
}

enum class CreditsPurchaseStatus {
    PENDING,
}

data class CreditsPurchaseEntry(
    val packId: String,
    val packName: String,
    val credits: Int,
    val status: CreditsPurchaseStatus,
    val formattedPrice: String,
    val creditsExpire: Boolean,
)

data class CreditsUiState(
    val stage: CreditsStage = CreditsStage.IDLE,
    val freeCredits: Int = 0,
    val purchasedCredits: Int = 0,
    val pendingPackIds: List<String> = emptyList(),
    val purchases: List<CreditsPurchaseEntry> = emptyList(),
    val failureReason: PurchaseFailureReason? = null,
    val isOffline: Boolean = false,
    val purchasedCreditsNeverExpire: Boolean = false,
    val purchasedCreditsMayExpire: Boolean = false,
) {
    val hasFreeCredits: Boolean
        get() = freeCredits > 0

    val hasPurchasedCredits: Boolean
        get() = purchasedCredits > 0

    val hasAnyCredits: Boolean
        get() = hasFreeCredits || hasPurchasedCredits

    val hasPurchases: Boolean
        get() = purchases.isNotEmpty()
    val hasPendingPurchase: Boolean
        get() = pendingPackIds.isNotEmpty()
}

fun creditsStageFor(scenario: DebugScenario): CreditsStage = when (scenario) {
    DebugScenario.LOADING -> CreditsStage.LOADING
    DebugScenario.ERROR -> CreditsStage.ERROR
    DebugScenario.OFFLINE -> CreditsStage.OFFLINE
    DebugScenario.EMPTY -> CreditsStage.ZERO
    DebugScenario.PURCHASED -> CreditsStage.PURCHASED_ONLY
    DebugScenario.SUCCESS -> CreditsStage.PURCHASED_ONLY
    DebugScenario.PARTIAL -> CreditsStage.MIXED
    DebugScenario.DELETING -> CreditsStage.RESTORING
    DebugScenario.DEFAULT -> CreditsStage.FREE_ONLY
    DebugScenario.SCANNED,
    DebugScenario.IMPORTED,
    DebugScenario.FULLY_CONFIRMED,
    DebugScenario.PARTLY_CONFIRMED,
    DebugScenario.USER_STATED,
    DebugScenario.EXPORTING,
    -> CreditsStage.FREE_ONLY
}

fun creditsIsStatic(scenario: DebugScenario): Boolean = scenario == DebugScenario.LOADING

fun creditsIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

fun creditsSeedsZero(scenario: DebugScenario): Boolean = scenario == DebugScenario.EMPTY

fun creditsSeedsMixed(scenario: DebugScenario): Boolean = scenario == DebugScenario.PARTIAL

fun creditsSeedsPurchasedOnly(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.PURCHASED || scenario == DebugScenario.SUCCESS

fun creditsFailsToLoad(scenario: DebugScenario): Boolean = scenario == DebugScenario.ERROR

internal fun creditsStageFor(entitlement: PurchaseEntitlement, isPending: Boolean): CreditsStage = when {
    isPending -> CreditsStage.PENDING
    !entitlement.hasAnyCredit() -> CreditsStage.ZERO
    entitlement.freeCredits > 0 && entitlement.purchasedCredits > 0 -> CreditsStage.MIXED
    entitlement.purchasedCredits > 0 -> CreditsStage.PURCHASED_ONLY
    else -> CreditsStage.FREE_ONLY
}

private fun PurchaseEntitlement.hasAnyCredit(): Boolean = freeCredits > 0 || purchasedCredits > 0

internal fun ApplicationPack.formattedPrice(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
    if (currency != null) {
        format.currency = currency
    }
    return format.format(BigDecimal.valueOf(priceInPaise, 2))
}
