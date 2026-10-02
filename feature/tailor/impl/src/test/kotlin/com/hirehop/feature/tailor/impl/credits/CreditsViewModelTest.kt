package com.hirehop.feature.tailor.impl.credits

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CreditsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gateway = TestPaymentGateway()
    private val connectivity = TestConnectivityMonitor()

    private fun viewModel() = CreditsViewModel(paymentGateway = gateway, connectivityMonitor = connectivity)

    private fun key(scenario: DebugScenario = DebugScenario.DEFAULT) = CreditsNavKey(scenario = scenario)

    @Test
    fun defaultScenario_showsTheFreeApplicationAndNoPurchases() = runTest {
        val subject = viewModel()

        subject.onEnter(key())

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.READY)
        assertThat(state.totalCredits).isEqualTo(1)
        assertThat(state.purchases).isEmpty()
        assertThat(state.showsFreeNote).isTrue()
        assertThat(state.creditsNeverExpire).isTrue()
    }

    @Test
    fun loadingScenario_staysLoading() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.LOADING))

        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.LOADING)
    }

    @Test
    fun errorScenario_isAnErrorWithARetry() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.ERROR))
        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.ERROR)

        subject.onAction(CreditsAction.Retry)

        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.READY)
    }

    @Test
    fun aCompletedPurchase_listsTheOrderWithPriceAndAddsTheCredits() = runTest {
        val subject = viewModel()
        subject.onEnter(key())

        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        val state = subject.uiState.value
        assertThat(state.totalCredits).isEqualTo(6)
        val entry = state.purchases.single()
        assertThat(entry.orderId).startsWith("mock-order-")
        assertThat(entry.credits).isEqualTo(5)
        assertThat(entry.formattedPrice).isEqualTo("₹149")
        assertThat(entry.isPending).isFalse()
        assertThat(state.showsFreeNote).isFalse()
        assertThat(state.refundOrderId).isEqualTo(entry.orderId)
    }

    @Test
    fun aPendingPurchase_isMarkedPendingAndAddsNoCredits() = runTest {
        gateway.withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Pending)
        val subject = viewModel()
        subject.onEnter(key())

        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        val state = subject.uiState.value
        assertThat(state.totalCredits).isEqualTo(1)
        assertThat(state.purchases.single().isPending).isTrue()
    }

    @Test
    fun theNewestPurchaseComesFirst() = runTest {
        val subject = viewModel()
        subject.onEnter(key())

        gateway.purchase(ApplicationPack.SINGLE_APPLICATION)
        gateway.purchase(ApplicationPack.APPLICATION_PACK_FIVE)

        assertThat(subject.uiState.value.purchases.map { entry -> entry.credits }).containsExactly(5, 1).inOrder()
    }

    @Test
    fun spendingACreditUpdatesTheNumber() = runTest {
        val subject = viewModel()
        subject.onEnter(key())

        gateway.consumeCredit()

        assertThat(subject.uiState.value.totalCredits).isEqualTo(0)
    }

    @Test
    fun offlineScenario_blocksBuyingButKeepsTheHistory() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.OFFLINE))

        val state = subject.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.canBuy).isFalse()
        assertThat(state.stage).isEqualTo(CreditsStage.READY)
    }

    @Test
    fun theConnectivityMonitorDecidesOffline() = runTest {
        connectivity.setOnline(false)
        val subject = viewModel()
        subject.onEnter(key())
        assertThat(subject.uiState.value.canBuy).isFalse()

        connectivity.setOnline(true)

        assertThat(subject.uiState.value.canBuy).isTrue()
    }
}
