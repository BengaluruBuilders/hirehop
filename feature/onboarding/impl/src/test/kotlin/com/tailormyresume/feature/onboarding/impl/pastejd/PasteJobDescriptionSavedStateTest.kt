package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.domain.ProposeJobLabelUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.repository.TestUsageAllowance
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PasteJobDescriptionSavedStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()

    private fun viewModel(savedState: SavedStateHandle) = PasteJobDescriptionViewModel(
        sessionRepository = session,
        nextOnboardingStep = NextOnboardingStepUseCase(session, TestProfileRepository()),
        connectivityMonitor = TestConnectivityMonitor(),
        usageAllowance = TestUsageAllowance(TestClock()),
        proposeJobLabel = ProposeJobLabelUseCase(NoLabelAnalyzer),
        discardJobDrafts = DiscardJobDraftsUseCase(TestPrepPlanRepository(), TestContentReportRepository()),
        computeDispatcher = UnconfinedTestDispatcher(),
        savedState = savedState,
    )

    private fun restarted(handle: SavedStateHandle) =
        SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })

    @Test
    fun typedJdCompanyAndRoleSurviveProcessDeath() = runTest {
        val handle = SavedStateHandle()
        val before = viewModel(handle)
        before.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        before.onAction(PasteJobDescriptionAction.TextChanged("Analyst role at Northwind"))
        before.onAction(PasteJobDescriptionAction.CompanyChanged("Northwind"))
        before.onAction(PasteJobDescriptionAction.RoleChanged("Analyst"))

        val after = viewModel(restarted(handle))
        after.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        val state = after.uiState.value
        assertThat(state.text).isEqualTo("Analyst role at Northwind")
        assertThat(state.company).isEqualTo("Northwind")
        assertThat(state.role).isEqualTo("Analyst")
    }

    @Test
    fun aClearedFormIsNotBroughtBack() = runTest {
        val handle = SavedStateHandle()
        val before = viewModel(handle)
        before.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        before.onAction(PasteJobDescriptionAction.TextChanged("Analyst role at Northwind"))
        before.onAction(PasteJobDescriptionAction.ClearTapped)

        val after = viewModel(restarted(handle))
        after.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(after.uiState.value.text).isEmpty()
    }

    private object NoLabelAnalyzer : JobDescriptionAnalyzer {
        override suspend fun analyze(rawText: String): JobDescription =
            JobDescription(title = "", company = "", rawText = rawText, requirements = emptyList())
    }
}
