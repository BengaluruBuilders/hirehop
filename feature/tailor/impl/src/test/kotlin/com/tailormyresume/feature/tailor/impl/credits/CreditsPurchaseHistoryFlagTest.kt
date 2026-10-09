package com.tailormyresume.feature.tailor.impl.credits

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseRecord
import com.tailormyresume.core.domain.PurchaseState
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.tailor.api.navigation.CreditsNavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class CreditsPurchaseHistoryFlagTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class GatewayWithoutPacks(
        private val delegate: TestPaymentGateway = TestPaymentGateway(),
    ) : PaymentGateway by delegate {
        override suspend fun packs(): List<ApplicationPack> = emptyList()

        override fun observePurchaseHistory(): Flow<List<PurchaseRecord>> = flowOf(
            listOf(
                PurchaseRecord(
                    packId = "retired",
                    orderId = "GPA.1",
                    purchasedAt = Instant.fromEpochSeconds(0),
                    state = PurchaseState.COMPLETED,
                ),
            ),
        )
    }

    @Test
    fun historyWhosePackIsMissing_stillMarksThePurchaseHistory() = runTest {
        val subject = CreditsViewModel(
            paymentGateway = GatewayWithoutPacks(),
            connectivityMonitor = TestConnectivityMonitor(),
        )

        subject.onEnter(CreditsNavKey(scenario = DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.READY)
        assertThat(state.purchases).isEmpty()
        assertThat(state.hasPurchaseHistory).isTrue()
    }
}
