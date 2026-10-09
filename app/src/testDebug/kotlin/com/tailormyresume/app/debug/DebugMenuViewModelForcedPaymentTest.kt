package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.ForcedPaymentScenario
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.sample.TestSampleDataController
import com.tailormyresume.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DebugMenuViewModelForcedPaymentTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val forced = ForcedPaymentScenario()
    private val connectivity = TestConnectivityMonitor()
    private fun viewModel() = DebugMenuViewModel(TestSampleDataController(), connectivity, connectivity, forced)

    @Test
    fun openingThePackScreenForcesTheChosenScenarioBeforeItShows() = runTest {
        var seenWhenReady: DebugScenario? = null

        viewModel().openPreview(DebugScenarioTarget.PackPurchase, DebugScenario.PENDING) { seenWhenReady = forced.scenario }

        assertThat(seenWhenReady).isEqualTo(DebugScenario.PENDING)
    }

    @Test
    fun openingCreditsForcesTheChosenScenario() = runTest {
        viewModel().openPreview(DebugScenarioTarget.Credits, DebugScenario.PENDING) {}

        assertThat(forced.scenario).isEqualTo(DebugScenario.PENDING)
    }

    @Test
    fun otherScreensForceNothing() = runTest {
        viewModel().openPreview(DebugScenarioTarget.Profile, DebugScenario.PENDING) {}

        assertThat(forced.scenario).isEqualTo(DebugScenario.DEFAULT)
    }

    @Test
    fun closingThePreviewClearsTheForcedScenario() = runTest {
        val subject = viewModel()
        subject.openPreview(DebugScenarioTarget.PackPurchase, DebugScenario.FAILED) {}

        subject.closePreview(DebugScenarioTarget.PackPurchase)

        assertThat(forced.scenario).isEqualTo(DebugScenario.DEFAULT)
    }

    @Test
    fun closingThePreviewIsSeenByCollectorsThatAreAlreadyRunning() = runTest {
        val subject = viewModel()
        subject.openPreview(DebugScenarioTarget.Credits, DebugScenario.PENDING) {}
        val seen = mutableListOf<DebugScenario>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) { forced.scenarios.collect { seen += it } }

        subject.closePreview(DebugScenarioTarget.Credits)
        collecting.cancel()

        assertThat(seen).containsExactly(DebugScenario.PENDING, DebugScenario.DEFAULT).inOrder()
    }
}
