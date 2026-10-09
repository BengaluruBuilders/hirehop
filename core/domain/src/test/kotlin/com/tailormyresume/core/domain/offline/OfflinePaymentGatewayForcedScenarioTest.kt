package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PurchaseResult
import com.tailormyresume.core.domain.PurchaseState
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.core.testing.util.TestIdGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflinePaymentGatewayForcedScenarioTest {

    private val forced = ForcedPaymentScenario()
    private val gateway = OfflinePaymentGateway(
        TestMockStateStore(),
        NoMockLatency,
        TestClock(),
        TestIdGenerator("order"),
        forced,
    )

    @Test
    fun aPendingScenarioMakesTheNextPurchasePending() = runTest {
        forced.scenario = DebugScenario.PENDING

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Pending::class.java)
    }

    @Test
    fun aCancelledScenarioMakesTheNextPurchaseCancelled() = runTest {
        forced.scenario = DebugScenario.CANCELLED

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isEqualTo(PurchaseResult.Cancelled)
    }

    @Test
    fun aFailedScenarioMakesTheNextPurchaseFail() = runTest {
        forced.scenario = DebugScenario.FAILED

        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Failed::class.java)
    }

    @Test
    fun theDefaultScenarioStillCompletesAPurchase() = runTest {
        val result = gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(result).isInstanceOf(PurchaseResult.Completed::class.java)
    }

    @Test
    fun aPendingScenarioShowsOnePendingPurchaseAndNoCredits() = runTest {
        forced.scenario = DebugScenario.PENDING

        val entitlement = gateway.observeEntitlement().first()
        val history = gateway.observePurchaseHistory().first()

        assertThat(entitlement.totalCredits).isEqualTo(0)
        assertThat(entitlement.pendingPackIds).containsExactly(ApplicationPack.APPLICATION_PACK_FIVE)
        assertThat(history.map { record -> record.state }).containsExactly(PurchaseState.PENDING)
    }

    @Test
    fun otherScenariosLeaveTheObservedStateAlone() = runTest {
        forced.scenario = DebugScenario.CANCELLED

        assertThat(gateway.observeEntitlement().first().totalCredits).isEqualTo(1)
        assertThat(gateway.observePurchaseHistory().first()).isEmpty()
    }
}
