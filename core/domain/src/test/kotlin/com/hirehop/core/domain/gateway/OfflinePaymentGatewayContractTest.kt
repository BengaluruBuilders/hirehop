package com.hirehop.core.domain.gateway

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.domain.PurchaseResult
import com.hirehop.core.domain.offline.OfflinePaymentGateway
import com.hirehop.core.testing.gateway.PaymentGatewayContractTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflinePaymentGatewayContractTest : PaymentGatewayContractTest() {

    override fun createPaymentGateway(): PaymentGateway = OfflinePaymentGateway()

    @Test
    fun theCatalogueHoldsTheTwoNonRenewingProducts() = runTest {
        val gateway = OfflinePaymentGateway()

        val packs = gateway.packs()

        assertThat(packs).containsExactly(
            ApplicationPack.applicationPackFive,
            ApplicationPack.singleApplication,
        ).inOrder()
    }

    @Test
    fun aNewAccountHasTheFreeAllowanceAndNoPurchasedCredits() = runTest {
        val gateway = OfflinePaymentGateway()

        val entitlement = gateway.entitlement()

        assertThat(entitlement.freeCredits).isEqualTo(1)
        assertThat(entitlement.purchasedCredits).isEqualTo(0)
        assertThat(entitlement.pendingPackIds).isEmpty()
    }

    @Test
    fun theSuccessOutcomeAddsTheCreditsOfTheBoughtPack() = runTest {
        val gateway = scripted(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Success)

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Completed::class.java)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(5)
    }

    @Test
    fun thePendingOutcomeAddsNoCreditsAndHoldsThePack() = runTest {
        val gateway = scripted(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Pending)

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Pending::class.java)
        val entitlement = gateway.entitlement()
        assertThat(entitlement.purchasedCredits).isEqualTo(0)
        assertThat(entitlement.pendingPackIds).containsExactly(ApplicationPack.APPLICATION_PACK_FIVE)
    }

    @Test
    fun aConfirmedPurchaseClearsAPendingHoldOnTheSamePack() = runTest {
        val gateway = scripted(ApplicationPack.SINGLE_APPLICATION, PurchaseOutcome.Pending)
        gateway.purchase(ApplicationPack.SINGLE_APPLICATION)
        assertThat(gateway.entitlement().pendingPackIds).isNotEmpty()

        gateway.withOutcome(ApplicationPack.SINGLE_APPLICATION, PurchaseOutcome.Success)
        val result = gateway.purchase(ApplicationPack.SINGLE_APPLICATION)

        assertThat(result).isInstanceOf(PurchaseResult.Completed::class.java)
        val entitlement = gateway.entitlement()
        assertThat(entitlement.pendingPackIds).isEmpty()
        assertThat(entitlement.purchasedCredits).isEqualTo(1)
    }

    @Test
    fun theCancelledOutcomeAddsNoCredits() = runTest {
        val gateway = scripted(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Cancelled)

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isEqualTo(PurchaseResult.Cancelled)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(0)
    }

    @Test
    fun theFailedOutcomeReportsAReasonAndAddsNoCredits() = runTest {
        val gateway = OfflinePaymentGateway()
            .withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Failed)
            .withFailureReason(PurchaseFailureReason.PaymentDeclined)

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Failed::class.java)
        assertThat((result as PurchaseResult.Failed).reason).isEqualTo(PurchaseFailureReason.PaymentDeclined)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(0)
    }

    @Test
    fun eachFailureReasonCanBeForced() = runTest {
        PurchaseFailureReason.entries.forEach { reason ->
            val gateway = OfflinePaymentGateway()
                .withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Failed)
                .withFailureReason(reason)

            val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE) as PurchaseResult.Failed

            assertThat(result.reason).isEqualTo(reason)
        }
    }

    @Test
    fun theSameInputAlwaysProducesTheSameResult() = runTest {
        val first = scripted(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Pending)
        val second = scripted(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Pending)

        assertThat(second.purchase(ApplicationPack.APPLICATION_PACK_FIVE))
            .isEqualTo(first.purchase(ApplicationPack.APPLICATION_PACK_FIVE))
    }

    @Test
    fun theOutcomeOfOnePackDoesNotChangeTheOther() = runTest {
        val gateway = scripted(ApplicationPack.SINGLE_APPLICATION, PurchaseOutcome.Pending)

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Completed::class.java)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(5)
    }

    @Test
    fun restoreReturnsWhatTheAccountAlreadyOwns() = runTest {
        val gateway = OfflinePaymentGateway()
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        gateway.purchase(ApplicationPack.SINGLE_APPLICATION)

        val restored = gateway.restorePurchases()

        assertThat(restored).isEqualTo(gateway.entitlement())
        assertThat(restored.purchasedCredits).isEqualTo(6)
    }

    @Test
    fun buyingTheSameConsumablePackTwiceGrantsItTwice() = runTest {
        val gateway = OfflinePaymentGateway()

        gateway.purchase(ApplicationPack.SINGLE_APPLICATION)
        gateway.purchase(ApplicationPack.SINGLE_APPLICATION)

        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(2)
    }

    private fun scripted(
        packId: String,
        outcome: PurchaseOutcome,
    ): OfflinePaymentGateway = OfflinePaymentGateway().withOutcome(packId, outcome)
}
