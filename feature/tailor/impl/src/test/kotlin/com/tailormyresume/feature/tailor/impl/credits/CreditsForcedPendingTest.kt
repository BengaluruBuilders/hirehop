package com.tailormyresume.feature.tailor.impl.credits

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.ForcedPaymentScenario
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.tailor.api.navigation.CreditsNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CreditsForcedPendingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun aForcedPendingPurchaseShowsZeroCreditsAndAPendingEntry() = runTest {
        val forced = ForcedPaymentScenario().apply { scenario = DebugScenario.PENDING }
        val subject = CreditsViewModel(TestPaymentGateway(forced = forced), TestConnectivityMonitor())

        subject.onEnter(CreditsNavKey(scenario = DebugScenario.PENDING))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.READY)
        assertThat(state.totalCredits).isEqualTo(0)
        assertThat(state.purchases.map { entry -> entry.isPending }).containsExactly(true)
    }
}
