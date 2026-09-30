package com.hirehop.feature.settings.impl.yourdata

import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseResult

class TestSettingsPaymentGateway(
    entitlement: PurchaseEntitlement = PurchaseEntitlement(
        freeCredits = 1,
        purchasedCredits = 0,
        pendingPackIds = emptyList(),
    ),
) : PaymentGateway {

    private var currentEntitlement: PurchaseEntitlement = entitlement

    private var shouldFail: Boolean = false

    fun withEntitlement(entitlement: PurchaseEntitlement): TestSettingsPaymentGateway =
        apply { currentEntitlement = entitlement }

    fun withFailure(): TestSettingsPaymentGateway = apply { shouldFail = true }

    override suspend fun packs(): List<ApplicationPack> {
        if (shouldFail) throw IllegalStateException("catalogue unavailable")
        return ApplicationPack.catalogue
    }

    override suspend fun purchase(packId: String): PurchaseResult = if (shouldFail) {
        PurchaseResult.Failed(
            reason = PurchaseFailureReason.PaymentUnavailable,
            entitlement = currentEntitlement,
        )
    } else {
        PurchaseResult.Completed(currentEntitlement)
    }

    override suspend fun entitlement(): PurchaseEntitlement {
        if (shouldFail) throw IllegalStateException("entitlement unavailable")
        return currentEntitlement
    }

    override suspend fun clearCredits(): PurchaseEntitlement {
        if (shouldFail) throw IllegalStateException("entitlement unavailable")
        currentEntitlement = PurchaseEntitlement(freeCredits = 0, purchasedCredits = 0, pendingPackIds = emptyList())
        return currentEntitlement
    }

    override suspend fun restorePurchases(): PurchaseEntitlement = entitlement()

    override suspend fun consumeCredit(): CreditSpend = if (currentEntitlement.totalCredits > 0) {
        CreditSpend.Spent(
            currentEntitlement.copy(freeCredits = currentEntitlement.freeCredits - 1),
        )
    } else {
        CreditSpend.NoCreditLeft
    }
}
