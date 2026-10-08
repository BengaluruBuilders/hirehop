package com.tailormyresume.feature.tailor.impl.packpurchase

import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat

internal enum class PackPurchaseStage {
    LOADING,
    READY,
    PURCHASING,
    PENDING,
    SUCCESS,
    CANCELLED,
    FAILED,
}

internal data class PackPurchaseReceipt(
    val credits: Int,
    val formattedPrice: String,
    val formattedDate: String,
)

internal data class PackPurchaseUiState(
    val stage: PackPurchaseStage = PackPurchaseStage.LOADING,
    val packs: List<ApplicationPack> = emptyList(),
    val selectedPackId: String = "",
    val jobTitle: String = "",
    val jobCompany: String = "",
    val totalCredits: Int = 0,
    val creditsBefore: Int = 0,
    val receipt: PackPurchaseReceipt? = null,
    val failureReason: PurchaseFailureReason? = null,
    val isOffline: Boolean = false,
    val hasApplication: Boolean = true,
    val format: ExportFormat = ExportFormat.PDF,
) {
    val selectedPack: ApplicationPack?
        get() = packs.firstOrNull { pack -> pack.id == selectedPackId }

    val otherPacks: List<ApplicationPack>
        get() = packs.filter { pack -> pack.id != selectedPackId }

    val hasCatalogue: Boolean get() = packs.isNotEmpty()

    val creditsNeverExpire: Boolean get() = packs.isNotEmpty() && packs.none { pack -> pack.creditsExpire }

    val isCatalogueFailure: Boolean get() = stage == PackPurchaseStage.FAILED && failureReason == null

    val canBuy: Boolean get() = stage == PackPurchaseStage.READY && !isOffline && selectedPack != null
}

internal fun packPurchaseIsStatic(scenario: DebugScenario): Boolean = scenario == DebugScenario.LOADING

internal fun packPurchaseIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

internal fun packPurchaseHidesCatalogue(scenario: DebugScenario): Boolean = scenario == DebugScenario.ERROR
