package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.ForcedPaymentScenario
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.sample.TestSampleDataController
import com.tailormyresume.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DebugMenuViewModelForcedPaymentTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val forced = ForcedPaymentScenario()
    private val connectivity = TestConnectivityMonitor()
    private val viewModel = DebugMenuViewModel(TestSampleDataController(), connectivity, connectivity, forced)

    @Test
    fun openingThePackScreenForcesTheChosenScenarioBeforeItShows() = runTest {
        var seenWhenReady: DebugScenario? = null

        viewModel.openPreview(DebugScenarioTarget.PackPurchase, DebugScenario.PENDING) { seenWhenReady = forced.scenario }

        assertThat(seenWhenReady).isEqualTo(DebugScenario.PENDING)
    }

    @Test
    fun openingCreditsForcesTheChosenScenario() = runTest {
        viewModel.openPreview(DebugScenarioTarget.Credits, DebugScenario.PENDING) {}

        assertThat(forced.scenario).isEqualTo(DebugScenario.PENDING)
    }

    @Test
    fun otherScreensForceNothing() = runTest {
        viewModel.openPreview(DebugScenarioTarget.Profile, DebugScenario.PENDING) {}

        assertThat(forced.scenario).isEqualTo(DebugScenario.DEFAULT)
    }

    @Test
    fun closingThePreviewClearsTheForcedScenario() = runTest {
        viewModel.openPreview(DebugScenarioTarget.PackPurchase, DebugScenario.FAILED) {}

        viewModel.closePreview(DebugScenarioTarget.PackPurchase)

        assertThat(forced.scenario).isEqualTo(DebugScenario.DEFAULT)
    }
}
