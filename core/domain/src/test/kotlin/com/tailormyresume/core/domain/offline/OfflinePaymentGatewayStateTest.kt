package com.tailormyresume.core.domain.offline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.MockLatency
import com.tailormyresume.core.data.mock.MockOperation
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.CreditSpend
import com.tailormyresume.core.domain.PurchaseOutcome
import com.tailormyresume.core.domain.PurchaseState
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.core.testing.util.TestIdGenerator
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
            unlock("application-1")
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
    fun anUnlockedApplicationStaysFreeAfterARestart() = runTest {
        gateway().unlock("application-a")

        val repeat = gateway().unlock("application-a") as CreditSpend.Spent
        val other = gateway().unlock("application-b")

        assertThat(repeat.kind).isNull()
        assertThat(repeat.entitlement.freeCredits).isEqualTo(0)
        assertThat(other).isEqualTo(CreditSpend.NoCreditLeft)
    }

    @Test
    fun spendingUsesTheFreeCreditFirstThenThePurchasedCredits() = runTest {
        val gateway = gateway()
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        val first = gateway.unlock("application-2") as CreditSpend.Spent
        val second = gateway.unlock("application-3") as CreditSpend.Spent
        repeat(4) { gateway.unlock("application-extra-$it") }
        val last = gateway.unlock("application-5")

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
            gateway.unlock("application-6")
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
        gateway.unlock("application-7")
        assertThat(gateway.withFreeCredits(9).entitlement().freeCredits).isEqualTo(3)
    }
}
