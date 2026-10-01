package com.hirehop.feature.tailor.impl.packpurchase

import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.model.DebugScenario
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class PackPurchaseStage {
    IDLE,
    LOADING_PACKS,
    READY,
    PURCHASING,
    PENDING,
    SUCCESS,
    CANCELLED,
    FAILED,
    RESTORING,
    RESTORED,
    OFFLINE,
}

data class PackPurchaseUiState(
    val stage: PackPurchaseStage = PackPurchaseStage.IDLE,
    val packs: List<ApplicationPack> = emptyList(),
    val selectedPackId: String = "",
    val entitlement: PurchaseEntitlement? = null,
    val failureReason: PurchaseFailureReason? = null,
    val isOffline: Boolean = false,
) {
    val selectedPack: ApplicationPack?
        get() = packs.firstOrNull { pack -> pack.id == selectedPackId }

    val otherPacks: List<ApplicationPack>
        get() = packs.filter { pack -> pack.id != selectedPackId }

    val freeCredits: Int
        get() = entitlement?.freeCredits ?: 0

    val purchasedCredits: Int
        get() = entitlement?.purchasedCredits ?: 0

    val pendingPackIds: List<String>
        get() = entitlement?.pendingPackIds ?: emptyList()

    val isBuying: Boolean
        get() = stage == PackPurchaseStage.PURCHASING

    val isRestoring: Boolean
        get() = stage == PackPurchaseStage.RESTORING

    val hasCatalogue: Boolean
        get() = packs.isNotEmpty()

    val purchasedCreditsNeverExpire: Boolean
        get() = packs.isNotEmpty() && packs.all { pack -> !pack.creditsExpire }

    val purchasedCreditsMayExpire: Boolean
        get() = packs.any { pack -> pack.creditsExpire }

    val isCatalogueFailure: Boolean
        get() = stage == PackPurchaseStage.FAILED && failureReason == null
}

fun packPurchaseStageFor(scenario: DebugScenario): PackPurchaseStage = when (scenario) {
    DebugScenario.LOADING -> PackPurchaseStage.LOADING_PACKS
    DebugScenario.ERROR -> PackPurchaseStage.FAILED
    DebugScenario.SUCCESS,
    DebugScenario.PURCHASED,
    -> PackPurchaseStage.SUCCESS
    DebugScenario.DELETING -> PackPurchaseStage.PURCHASING
    DebugScenario.OFFLINE -> PackPurchaseStage.OFFLINE
    DebugScenario.EMPTY,
    DebugScenario.PARTIAL,
    DebugScenario.DEFAULT,
    -> PackPurchaseStage.READY
    else -> PackPurchaseStage.READY
}

fun packPurchaseIsStatic(scenario: DebugScenario): Boolean = scenario == DebugScenario.LOADING

fun packPurchaseIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

fun packPurchaseHidesCatalogue(scenario: DebugScenario): Boolean = scenario == DebugScenario.ERROR

fun packPurchaseShowsNoPacks(scenario: DebugScenario): Boolean = scenario == DebugScenario.EMPTY

fun packPurchaseSeedsRecordedPurchase(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.SUCCESS || scenario == DebugScenario.PURCHASED

fun packPurchaseSeedsPartlyUsed(scenario: DebugScenario): Boolean = scenario == DebugScenario.PARTIAL

internal fun ApplicationPack.formattedPrice(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
    if (currency != null) {
        format.currency = currency
    }
    return format.format(BigDecimal.valueOf(priceInPaise, 2))
}
