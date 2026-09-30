package com.hirehop.feature.tailor.impl.packpurchase

import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TestPaymentGateway : PaymentGateway {

    private val mutex = Mutex()

    private val catalogue: List<ApplicationPack> = ApplicationPack.catalogue

    private var catalogueOverride: List<ApplicationPack>? = null

    private var entitlement = PurchaseEntitlement(
        freeCredits = DEFAULT_FREE_CREDITS,
        purchasedCredits = 0,
        pendingPackIds = emptyList(),
    )

    private var nextResult: PurchaseResult? = null

    private var packsFailure: Boolean = false

    private var entitlementFailure: Boolean = false

    private var restoreFailure: Boolean = false

    private var purchaseCalls: Int = 0

    private var restoreCalls: Int = 0

    private var consumeCalls: Int = 0

    private var consumeFailure: Boolean = false

    fun consumeCallCount(): Int = consumeCalls

    fun withFreeCredits(credits: Int): TestPaymentGateway = apply { entitlement = entitlement.copy(freeCredits = credits) }

    fun withPurchasedCredits(credits: Int): TestPaymentGateway =
        apply { entitlement = entitlement.copy(purchasedCredits = credits) }

    fun withPendingPackIds(packIds: List<String>): TestPaymentGateway =
        apply { entitlement = entitlement.copy(pendingPackIds = packIds) }

    fun withResult(result: PurchaseResult): TestPaymentGateway = apply { nextResult = result }

    fun withPacksFailure(): TestPaymentGateway = apply { packsFailure = true }

    fun withEntitlementFailure(): TestPaymentGateway = apply { entitlementFailure = true }

    fun withRestoreFailure(): TestPaymentGateway = apply { restoreFailure = true }

    fun withConsumeFailure(): TestPaymentGateway = apply { consumeFailure = true }

    fun withEmptyCatalogue(): TestPaymentGateway = apply { catalogueOverride = emptyList() }

    fun purchaseCallCount(): Int = purchaseCalls

    fun restoreCallCount(): Int = restoreCalls

    override suspend fun packs(): List<ApplicationPack> = mutex.withLock {
        if (packsFailure) throw IllegalStateException("catalogue unavailable")
        catalogueOverride ?: catalogue
    }

    override suspend fun purchase(packId: String): PurchaseResult = mutex.withLock {
        purchaseCalls += 1
        val scripted = nextResult
        if (scripted != null) return@withLock scripted
        val pack = (catalogueOverride ?: catalogue).firstOrNull { candidate -> candidate.id == packId }
            ?: return@withLock PurchaseResult.Failed(
                reason = PurchaseFailureReason.PurchaseUnavailable,
                entitlement = entitlement,
            )
        entitlement = entitlement.copy(purchasedCredits = entitlement.purchasedCredits + pack.credits)
        PurchaseResult.Completed(entitlement)
    }

    override suspend fun entitlement(): PurchaseEntitlement = mutex.withLock {
        if (entitlementFailure) throw IllegalStateException("entitlement unavailable")
        entitlement
    }

    override suspend fun restorePurchases(): PurchaseEntitlement = mutex.withLock {
        restoreCalls += 1
        if (restoreFailure) throw IllegalStateException("restore unavailable")
        entitlement
    }

    override suspend fun consumeCredit(): CreditSpend = mutex.withLock {
        consumeCalls += 1
        if (consumeFailure) throw IllegalStateException("consume unavailable")
        when {
            entitlement.freeCredits > 0 -> {
                entitlement = entitlement.copy(freeCredits = entitlement.freeCredits - 1)
                CreditSpend.Spent(entitlement)
            }

            entitlement.purchasedCredits > 0 -> {
                entitlement = entitlement.copy(purchasedCredits = entitlement.purchasedCredits - 1)
                CreditSpend.Spent(entitlement)
            }

            else -> CreditSpend.NoCreditLeft
        }
    }

    private companion object {
        const val DEFAULT_FREE_CREDITS = 1
    }
}
