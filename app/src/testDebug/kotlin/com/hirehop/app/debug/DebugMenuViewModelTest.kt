package com.hirehop.app.debug

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.app.R
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.sample.TestSampleDataController
import com.hirehop.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DebugMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleData = TestSampleDataController()
    private val connectivity = TestConnectivityMonitor()

    private fun viewModel() = DebugMenuViewModel(sampleData, connectivity, connectivity)

    @Test
    fun loadSampleData_callsTheControllerAndReportsIt() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.loadSampleData()
            assertThat(expectMostRecentItem().message).isEqualTo(DebugDataMessage.SampleLoaded)
        }
        assertThat(sampleData.loadCount).isEqualTo(1)
    }

    @Test
    fun resetAppData_callsTheControllerAndReportsIt() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.resetAppData()
            assertThat(expectMostRecentItem().message).isEqualTo(DebugDataMessage.DataReset)
        }
        assertThat(sampleData.resetCount).isEqualTo(1)
    }

    @Test
    fun setOnline_reachesTheConnectivityControlAndTheState() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertThat(awaitItem().online).isTrue()
            viewModel.setOnline(false)
            assertThat(awaitItem().online).isFalse()
        }
    }

    @Test
    fun setOnline_changesTheConnectivityLabel() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertThat(awaitItem().connectivityLabel).isEqualTo(R.string.debug_connectivity_online)
            viewModel.setOnline(false)
            assertThat(awaitItem().connectivityLabel).isEqualTo(R.string.debug_connectivity_offline)
            viewModel.setOnline(true)
            assertThat(awaitItem().connectivityLabel).isEqualTo(R.string.debug_connectivity_online)
        }
    }

    @Test
    fun openPreview_ofGapAnalysis_keepsTheSampleJobBeforeItOpens() = runTest {
        var opened = false

        viewModel().openPreview(DebugScenarioTarget.Analysis) { opened = sampleData.sampleJobKept }

        assertThat(opened).isTrue()
    }

    @Test
    fun openPreview_ofAnotherScreen_keepsNoJob() = runTest {
        var opened = false

        viewModel().openPreview(DebugScenarioTarget.Profile) { opened = true }

        assertThat(opened).isTrue()
        assertThat(sampleData.sampleJobKept).isFalse()
    }

    @Test
    fun closePreview_ofGapAnalysis_removesTheSampleJob() = runTest {
        val viewModel = viewModel()
        viewModel.openPreview(DebugScenarioTarget.Analysis) {}

        viewModel.closePreview(DebugScenarioTarget.Analysis)

        assertThat(sampleData.sampleJobKept).isFalse()
    }
}
