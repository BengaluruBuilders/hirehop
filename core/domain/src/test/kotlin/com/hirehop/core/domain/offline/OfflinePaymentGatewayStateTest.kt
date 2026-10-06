package com.hirehop.core.domain.offline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.mock.MockLatency
import com.hirehop.core.data.mock.MockOperation
import com.hirehop.core.data.mock.NoMockLatency
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.domain.PurchaseState
import com.hirehop.core.model.CreditKind
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.util.TestClock
import com.hirehop.core.testing.util.TestIdGenerator
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class OfflinePaymentGatewayStateTest {

    private val store = TestMockStateStore()
    private val clock = TestClock(Instant.fromEpochMilliseconds(1_790_000_000_000))
    private val awaited = mutableListOf<MockOperation>()
    private val latency = object : MockLatency {
        override suspend fun await(operation: MockOperation) {
            awaited += operation
        }
    }

    private fun gateway(latency: MockLatency = NoMockLatency) =
        OfflinePaymentGateway(store, latency, clock, TestIdGenerator("order"))

    @Test
    fun creditsAndHistorySurviveARestart() = runTest {
        gateway().apply {
            purchase(ApplicationPack.APPLICATION_PACK_FIVE)
            consumeCredit()
        }

        val restarted = gateway()

        assertThat(restarted.entitlement().freeCredits).isEqualTo(0)
        assertThat(restarted.entitlement().purchasedCredits).isEqualTo(5)
        assertThat(restarted.purchaseHistory()).hasSize(1)
    }

    @Test
    fun aFreshGatewayStartsWithOneFreeCredit() = runTest {
        assertThat(gateway().entitlement().freeCredits).isEqualTo(1)
    }

    @Test
    fun aPurchaseIsRecordedWithTheOrderIdAndTime() = runTest {
        val gateway = gateway()

        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        val record = gateway.purchaseHistory().single()
        assertThat(record.packId).isEqualTo(ApplicationPack.APPLICATION_PACK_FIVE)
        assertThat(record.orderId).isEqualTo("mock-order-order-1")
        assertThat(record.purchasedAt).isEqualTo(clock.instant)
        assertThat(record.state).isEqualTo(PurchaseState.COMPLETED)
    }

    @Test
    fun aPendingPurchaseIsRecordedOnceAndBecomesCompletedWhenItSettles() = runTest {
        val gateway = gateway().withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Pending)
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        assertThat(gateway.purchaseHistory().map { it.state }).containsExactly(PurchaseState.PENDING)

        gateway.withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Success)
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(gateway.purchaseHistory().map { it.state }).containsExactly(PurchaseState.COMPLETED)
    }

    @Test
    fun spendingUsesTheFreeCreditFirstThenThePurchasedCredits() = runTest {
        val gateway = gateway()
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        val first = gateway.consumeCredit() as CreditSpend.Spent
        val second = gateway.consumeCredit() as CreditSpend.Spent
        repeat(4) { gateway.consumeCredit() }
        val last = gateway.consumeCredit()

        assertThat(first.kind).isEqualTo(CreditKind.FREE)
        assertThat(second.kind).isEqualTo(CreditKind.PURCHASED)
        assertThat(last).isEqualTo(CreditSpend.NoCreditLeft)
    }

    @Test
    fun observedEntitlementFollowsPurchasesAndSpends() = runTest {
        val gateway = gateway()

        gateway.observeEntitlement().test {
            assertThat(awaitItem().totalCredits).isEqualTo(1)
            gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
            assertThat(awaitItem().totalCredits).isEqualTo(6)
            gateway.consumeCredit()
            assertThat(awaitItem().totalCredits).isEqualTo(5)
        }
    }

    @Test
    fun observedHistoryFollowsPurchases() = runTest {
        val gateway = gateway()

        gateway.observePurchaseHistory().test {
            assertThat(awaitItem()).isEmpty()
            gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun clearingCreditsRemovesTheHistoryAndTheBalance() = runTest {
        val gateway = gateway()
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        gateway.clearCredits()

        assertThat(gateway.entitlement().totalCredits).isEqualTo(0)
        assertThat(gateway.purchaseHistory()).isEmpty()
    }

    @Test
    fun theGatewayWaitsForTheLatencyPolicyOnSlowOperations() = runTest {
        val gateway = gateway(latency)

        gateway.packs()
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        gateway.restorePurchases()

        assertThat(awaited).containsExactly(
            MockOperation.LOAD_PACKS,
            MockOperation.PURCHASE,
            MockOperation.RESTORE,
        ).inOrder()
    }

    @Test
    fun aStartingFreeCreditCountOnlyAppliesBeforeAnyStateIsSaved() = runTest {
        val gateway = gateway().withFreeCredits(4)

        assertThat(gateway.entitlement().freeCredits).isEqualTo(4)
        gateway.consumeCredit()
        assertThat(gateway.withFreeCredits(9).entitlement().freeCredits).isEqualTo(3)
    }
}
