package com.hirehop.core.domain.account

import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import javax.inject.Inject

class AccountCreditBalance @Inject constructor(
    private val paymentGateway: PaymentGateway,
) {

    suspend fun unusedCredits(): Int = paymentGateway.entitlement().totalCredits

    suspend fun clearUnusedCredits() {
        var attempts = 0
        while (attempts < MAX_CLEAR_ATTEMPTS && unusedCredits() > 0) {
            val spend = paymentGateway.consumeCredit()
            attempts += 1
            if (spend is CreditSpend.NoCreditLeft) break
        }
        check(unusedCredits() == 0) { CREDIT_CLEAR_FAILED }
    }
}

private const val MAX_CLEAR_ATTEMPTS = 64

private const val CREDIT_CLEAR_FAILED = "The credit balance did not reach zero."
