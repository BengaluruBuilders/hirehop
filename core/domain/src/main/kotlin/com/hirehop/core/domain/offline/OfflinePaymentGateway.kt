package com.hirehop.core.domain.offline

import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.domain.PurchaseResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class OfflinePaymentGateway @Inject constructor() : PaymentGateway {

    private val mutex = Mutex()

    private val catalogue: List<ApplicationPack> = ApplicationPack.catalogue
    private val scriptedOutcomes = mutableMapOf<String, PurchaseOutcome>()
    private val confirmedPackIds = mutableListOf<String>()
    private val pendingPackIds = mutableListOf<String>()
    private var freeCredits: Int = DEFAULT_FREE_CREDITS
    private var spentPurchasedCredits: Int = 0
    private var failureReason: PurchaseFailureReason = PurchaseFailureReason.PaymentUnavailable

    fun withOutcome(
        packId: String,
        outcome: PurchaseOutcome,
    ): OfflinePaymentGateway = apply { scriptedOutcomes[packId] = outcome }

    fun withFailureReason(reason: PurchaseFailureReason): OfflinePaymentGateway = apply {
        failureReason = reason
    }

    fun withFreeCredits(credits: Int): OfflinePaymentGateway = apply { freeCredits = credits }

    override suspend fun packs(): List<ApplicationPack> = mutex.withLock { catalogue }

    override suspend fun purchase(packId: String): PurchaseResult = mutex.withLock {
        val pack = catalogue.find { candidate -> candidate.id == packId }
            ?: return@withLock PurchaseResult.Failed(
                reason = PurchaseFailureReason.PurchaseUnavailable,
                entitlement = currentEntitlement(),
            )
        when (outcomeFor(pack.id)) {
            PurchaseOutcome.Success -> confirm(pack)
            PurchaseOutcome.Pending -> hold(pack)
            PurchaseOutcome.Cancelled -> PurchaseResult.Cancelled
            PurchaseOutcome.Failed -> PurchaseResult.Failed(reason = failureReason, entitlement = currentEntitlement())
        }
    }

    override suspend fun entitlement(): PurchaseEntitlement = mutex.withLock { currentEntitlement() }

    override suspend fun restorePurchases(): PurchaseEntitlement = mutex.withLock { currentEntitlement() }

    override suspend fun consumeCredit(): CreditSpend = mutex.withLock {
        when {
            freeCredits > 0 -> {
                freeCredits -= 1
                CreditSpend.Spent(currentEntitlement())
            }

            purchasedCredits() > 0 -> {
                spentPurchasedCredits += 1
                CreditSpend.Spent(currentEntitlement())
            }

            else -> CreditSpend.NoCreditLeft
        }
    }

    private fun purchasedCredits(): Int =
        (confirmedPackIds.sumOf { id -> creditsOf(id) } - spentPurchasedCredits).coerceAtLeast(0)

    override suspend fun clearCredits(): PurchaseEntitlement = mutex.withLock {
        freeCredits = 0
        spentPurchasedCredits = confirmedPackIds.sumOf { id -> creditsOf(id) }
        pendingPackIds.clear()
        currentEntitlement()
    }

    private fun outcomeFor(packId: String): PurchaseOutcome = scriptedOutcomes[packId] ?: PurchaseOutcome.Success

    private fun confirm(pack: ApplicationPack): PurchaseResult {
        confirmedPackIds += pack.id
        pendingPackIds -= pack.id
        return PurchaseResult.Completed(currentEntitlement())
    }

    private fun hold(pack: ApplicationPack): PurchaseResult {
        if (pack.id !in pendingPackIds) pendingPackIds += pack.id
        return PurchaseResult.Pending(currentEntitlement())
    }

    private fun currentEntitlement(): PurchaseEntitlement = PurchaseEntitlement(
        freeCredits = freeCredits,
        purchasedCredits = purchasedCredits(),
        pendingPackIds = pendingPackIds.toList(),
    )

    private fun creditsOf(packId: String): Int = catalogue.find { pack -> pack.id == packId }?.credits ?: 0

    private companion object {
        const val DEFAULT_FREE_CREDITS = 1
    }
}
