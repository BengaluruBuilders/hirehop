package com.hirehop.core.domain.account

import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import javax.inject.Inject

class AccountCreditBalance @Inject constructor(
    private val paymentGateway: PaymentGateway,
) {

    suspend fun unusedCredits(): Int = paymentGateway.entitlement().totalCredits

    suspend fun creditLine(): AccountCreditLine {
        val entitlement = paymentGateway.entitlement()
        return AccountCreditLine(
            freeCredits = entitlement.freeCredits,
            purchasedCredits = entitlement.purchasedCredits,
        )
    }

    suspend fun clearUnusedCredits(): PurchaseEntitlement {
        val cleared = paymentGateway.clearCredits()
        check(cleared.totalCredits == 0) { CREDIT_CLEAR_FAILED }
        return cleared
    }
}

private const val CREDIT_CLEAR_FAILED = "The credit balance did not reach zero."
