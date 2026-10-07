package com.hirehop.core.domain.account

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseResult
import com.hirehop.core.testing.gateway.TestPaymentGateway
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AccountCreditBalanceTest {

    @Test
    fun unusedCreditsReadsTheRealGatewayBalance() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(3)
        val balance = AccountCreditBalance(paymentGateway = gateway)

        assertThat(balance.unusedCredits()).isEqualTo(3)
    }

    @Test
    fun unusedCreditsCountsPurchasedCreditsToo() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(0)
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        val balance = AccountCreditBalance(paymentGateway = gateway)

        assertThat(balance.unusedCredits()).isEqualTo(5)
    }

    @Test
    fun clearingTakesTheRealBalanceToZero() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(0)
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        val balance = AccountCreditBalance(paymentGateway = gateway)

        balance.clearUnusedCredits()

        assertThat(gateway.entitlement().totalCredits).isEqualTo(0)
    }

    @Test
    fun clearingAnAlreadyEmptyBalanceDoesNothing() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(0)
        val balance = AccountCreditBalance(paymentGateway = gateway)

        balance.clearUnusedCredits()

        assertThat(gateway.entitlement().totalCredits).isEqualTo(0)
    }

    @Test
    fun clearingAGatewayThatNeverDropsFailsInsteadOfClaimingSuccess() = runTest {
        val balance = AccountCreditBalance(paymentGateway = StuckPaymentGateway())

        val thrown = runCatching { balance.clearUnusedCredits() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
    }
}

private class StuckPaymentGateway : PaymentGateway {

    override suspend fun packs(): List<ApplicationPack> = emptyList()

    override suspend fun purchase(packId: String): PurchaseResult = PurchaseResult.Failed(
        reason = PurchaseFailureReason.PurchaseUnavailable,
        entitlement = entitlement(),
    )

    override suspend fun entitlement(): PurchaseEntitlement = PurchaseEntitlement(
        freeCredits = 4,
        purchasedCredits = 0,
        pendingPackIds = emptyList(),
    )

    override suspend fun restorePurchases(): PurchaseEntitlement = entitlement()

    override suspend fun unlock(applicationId: String): CreditSpend = CreditSpend.Spent(entitlement())

    override suspend fun clearCredits(): PurchaseEntitlement = entitlement()
}
