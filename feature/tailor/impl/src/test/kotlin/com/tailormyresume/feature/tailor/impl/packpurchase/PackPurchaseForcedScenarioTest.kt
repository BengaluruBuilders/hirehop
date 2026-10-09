package com.tailormyresume.feature.tailor.impl.packpurchase

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.offline.ForcedPaymentScenario
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.PackPurchaseNavKey
import com.tailormyresume.feature.tailor.impl.exportpreview.PendingExportStart
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PackPurchaseForcedScenarioTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buyUnder(scenario: DebugScenario): PackPurchaseUiState {
        val applications = TestApplicationRepository().apply { sendApplications(listOf(canonicalApplication)) }
        val subject = PackPurchaseViewModel(
            paymentGateway = TestPaymentGateway(forced = ForcedPaymentScenario().apply { this.scenario = scenario }),
            applicationRepository = applications,
            connectivityMonitor = TestConnectivityMonitor(),
            pendingExportStart = PendingExportStart(),
            clock = TestClock(),
        )
        subject.onEnter(PackPurchaseNavKey(applicationId = canonicalApplication.id, scenario = scenario))
        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))
        return subject.uiState.value
    }

    @Test
    fun buyingWhilePendingIsForcedEndsPending() = runTest {
        assertThat(buyUnder(DebugScenario.PENDING).stage).isEqualTo(PackPurchaseStage.PENDING)
    }

    @Test
    fun buyingWhileCancelledIsForcedEndsCancelledWithNoCreditsAdded() = runTest {
        val state = buyUnder(DebugScenario.CANCELLED)

        assertThat(state.stage).isEqualTo(PackPurchaseStage.CANCELLED)
        assertThat(state.totalCredits).isEqualTo(1)
    }

    @Test
    fun buyingWhileFailedIsForcedEndsFailedWithAPaymentReason() = runTest {
        val state = buyUnder(DebugScenario.FAILED)

        assertThat(state.stage).isEqualTo(PackPurchaseStage.FAILED)
        assertThat(state.failureReason).isNotNull()
        assertThat(state.isCatalogueFailure).isFalse()
    }
}
