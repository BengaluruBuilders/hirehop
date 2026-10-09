package com.tailormyresume.feature.tailor.impl.credits

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseRecord
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.tailor.api.navigation.CreditsNavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private class PurchasesFailGateway(delegate: PaymentGateway) : PaymentGateway by delegate {
    override fun observePurchaseHistory(): Flow<List<PurchaseRecord>> = flow { throw IOException("offline") }
}

class CreditsPurchaseFailureTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun aFailedPurchaseHistoryStillShowsReadyWithTheCreditCountAndNoVerdictOnPurchases() = runTest {
        val subject = CreditsViewModel(PurchasesFailGateway(TestPaymentGateway()), TestConnectivityMonitor())

        subject.onEnter(CreditsNavKey(scenario = DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.READY)
        assertThat(state.totalCredits).isEqualTo(1)
        assertThat(state.purchases).isEmpty()
        assertThat(state.purchasesKnown).isFalse()
    }

    @Test
    fun aPayingUserWhoseHistoryFailsToLoadStillGetsTheRefundRoute() = runTest {
        val gateway = TestPaymentGateway()
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)
        val subject = CreditsViewModel(PurchasesFailGateway(gateway), TestConnectivityMonitor())

        subject.onEnter(CreditsNavKey(scenario = DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.purchasedCredits).isGreaterThan(0)
        assertThat(state.offersRefund).isTrue()
        assertThat(state.purchasesKnown).isFalse()
    }

    @Test
    fun aKnownEmptyHistoryStillHidesTheRefundRoute() = runTest {
        val subject = CreditsViewModel(TestPaymentGateway(), TestConnectivityMonitor())

        subject.onEnter(CreditsNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(subject.uiState.value.offersRefund).isFalse()
    }
}
