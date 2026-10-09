package com.tailormyresume.feature.profile.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleEducationEntry
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.data.sampleProjectEntry
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScenarioTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        viewModel = ProfileViewModel(repository, TestSessionRepository(), TestConnectivityMonitor())
    }

    private val confirmed = sampleProfile.copy(
        entries = listOf(sampleEducationEntry.copy(isConfirmed = true), sampleProjectEntry.copy(isConfirmed = true)),
    )

    private suspend fun unconfirmedCountIn(scenario: DebugScenario): Int {
        repository.sendProfile(confirmed)
        viewModel.selectScenario(scenario)
        var count = -1
        viewModel.uiState.test { count = (expectMostRecentItem() as ProfileUiState.Success).overview.unconfirmedCount }
        return count
    }

    @Test
    fun partlyConfirmedScenarioShowsTwoPendingFacts() = runTest {
        assertThat(unconfirmedCountIn(DebugScenario.PARTLY_CONFIRMED)).isEqualTo(2)
    }

    @Test
    fun pendingScenarioShowsPendingFacts() = runTest {
        assertThat(unconfirmedCountIn(DebugScenario.PENDING)).isEqualTo(2)
    }

    @Test
    fun defaultScenarioLeavesTheStoredFactsAlone() = runTest {
        assertThat(unconfirmedCountIn(DebugScenario.DEFAULT)).isEqualTo(0)
    }
}
