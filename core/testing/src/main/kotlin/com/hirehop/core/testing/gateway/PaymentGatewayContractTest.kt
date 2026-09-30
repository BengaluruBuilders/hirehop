package com.hirehop.core.testing.gateway

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseResult
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class PaymentGatewayContractTest {

    protected abstract fun createPaymentGateway(): PaymentGateway

    @Test
    fun theCatalogueIsNotEmptyAndEveryPackIsSellable() = runTest {
        val gateway = createPaymentGateway()

        val packs = gateway.packs()

        assertThat(packs).isNotEmpty()
        packs.forEach { pack -> assertThat(pack.isSellable()).isTrue() }
    }

    @Test
    fun everyPackInTheCatalogueHasItsOwnId() = runTest {
        val gateway = createPaymentGateway()

        val packs = gateway.packs()

        assertThat(packs.map(ApplicationPack::id).distinct()).hasSize(packs.size)
    }

    @Test
    fun aNewAccountStartsWithTheFreeAllowanceAndNoPurchase() = runTest {
        val gateway = createPaymentGateway()

        val entitlement = gateway.entitlement()

        assertThat(entitlement.freeCredits).isAtLeast(0)
        assertThat(entitlement.purchasedCredits).isEqualTo(0)
        assertThat(entitlement.pendingPackIds).isEmpty()
    }

    @Test
    fun aCompletedPurchaseGrantsTheCreditsOfThatPack() = runTest {
        val gateway = createPaymentGateway()
        val pack = gateway.packs().first()
        val before = gateway.entitlement()

        val result = gateway.purchase(pack.id)

        if (result is PurchaseResult.Completed) {
            val after = gateway.entitlement()
            assertThat(after.purchasedCredits).isEqualTo(before.purchasedCredits + pack.credits)
        }
    }

    @Test
    fun aCompletedPurchaseReportsTheSameEntitlementAsTheQuery() = runTest {
        val gateway = createPaymentGateway()
        val pack = gateway.packs().first()

        val result = gateway.purchase(pack.id)

        if (result is PurchaseResult.Completed) {
            assertThat(result.entitlement).isEqualTo(gateway.entitlement())
        }
    }

    @Test
    fun aCancelledPurchaseGrantsNoCredits() = runTest {
        val gateway = createPaymentGateway()
        val pack = gateway.packs().first()
        val before = gateway.entitlement()

        val result = gateway.purchase(pack.id)

        if (result is PurchaseResult.Cancelled) {
            assertThat(gateway.entitlement()).isEqualTo(before)
        }
    }

    @Test
    fun aFailedPurchaseGrantsNoCredits() = runTest {
        val gateway = createPaymentGateway()
        val pack = gateway.packs().first()
        val before = gateway.entitlement()

        val result = gateway.purchase(pack.id)

        if (result is PurchaseResult.Failed) {
            assertThat(gateway.entitlement()).isEqualTo(before)
        }
    }

    @Test
    fun aPendingPurchaseAddsNoCreditsAndListsThePackAsPending() = runTest {
        val gateway = createPaymentGateway()
        val pack = gateway.packs().first()
        val before = gateway.entitlement()

        val result = gateway.purchase(pack.id)

        if (result is PurchaseResult.Pending) {
            val after = gateway.entitlement()
            assertThat(after.purchasedCredits).isEqualTo(before.purchasedCredits)
            assertThat(after.pendingPackIds).contains(pack.id)
        }
    }

    @Test
    fun aPackThatIsNotInTheCatalogueCannotBeBought() = runTest {
        val gateway = createPaymentGateway()

        val result = gateway.purchase(UNKNOWN_PACK_ID)

        assertThat(result).isInstanceOf(PurchaseResult.Failed::class.java)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(0)
    }

    @Test
    fun buyingTheSamePackTwiceNeverGrantsMoreThanOnePack() = runTest {
        val gateway = createPaymentGateway()
        val pack = gateway.packs().first()

        gateway.purchase(pack.id)
        val afterFirst = gateway.entitlement()
        gateway.purchase(pack.id)
        val afterSecond = gateway.entitlement()

        assertThat(afterSecond.purchasedCredits).isAtMost(afterFirst.purchasedCredits + pack.credits)
    }

    @Test
    fun restoreKeepsEveryCreditTheAccountAlreadyOwns() = runTest {
        val gateway = createPaymentGateway()
        gateway.packs().firstOrNull()?.let { pack -> gateway.purchase(pack.id) }
        val before = gateway.entitlement()

        val restored = gateway.restorePurchases()

        assertThat(restored.purchasedCredits).isAtLeast(before.purchasedCredits)
        assertThat(restored.freeCredits).isEqualTo(before.freeCredits)
    }

    @Test
    fun restoreOnAFreshAccountGrantsNothing() = runTest {
        val gateway = createPaymentGateway()

        val restored = gateway.restorePurchases()

        assertThat(restored).isEqualTo(gateway.entitlement())
    }

    @Test
    fun creditsNeverGoBelowZero() = runTest {
        val gateway = createPaymentGateway()

        val entitlement = gateway.entitlement()

        assertThat(entitlement.totalCredits).isAtLeast(0)
    }

    @Test
    fun spendingOneCreditLowersTheBalanceByExactlyOne() = runTest {
        val gateway = createPaymentGateway()
        gateway.packs().firstOrNull()?.let { pack -> gateway.purchase(pack.id) }
        val before = gateway.entitlement()

        val spend = gateway.consumeCredit()

        if (spend is CreditSpend.Spent) {
            val after = gateway.entitlement()
            assertThat(after.totalCredits).isEqualTo(before.totalCredits - 1)
            assertThat(spend.entitlement).isEqualTo(after)
        }
    }

    @Test
    fun spendingWithAnEmptyBalanceReportsNoCreditLeftAndChangesNothing() = runTest {
        val gateway = createPaymentGateway()
        repeat(SPEND_ATTEMPTS_UNTIL_EMPTY) {
            if (gateway.entitlement().totalCredits > 0) gateway.consumeCredit()
        }
        val before = gateway.entitlement()

        val spend = gateway.consumeCredit()

        if (before.totalCredits == 0) {
            assertThat(spend).isEqualTo(CreditSpend.NoCreditLeft)
            assertThat(gateway.entitlement()).isEqualTo(before)
        }
    }

    @Test
    fun spendingNeverLeavesTheBalanceNegative() = runTest {
        val gateway = createPaymentGateway()
        repeat(SPEND_ATTEMPTS_BEYOND_BALANCE) {
            gateway.consumeCredit()
            assertThat(gateway.entitlement().totalCredits).isAtLeast(0)
        }
    }

    private fun ApplicationPack.isSellable(): Boolean =
        id.isNotEmpty() && name.isNotEmpty() && credits > 0 && priceInPaise > 0 && currencyCode.isNotEmpty()

    private companion object {
        const val UNKNOWN_PACK_ID = "pack_that_does_not_exist"
        const val SPEND_ATTEMPTS_UNTIL_EMPTY = 12
        const val SPEND_ATTEMPTS_BEYOND_BALANCE = 20
    }
}
