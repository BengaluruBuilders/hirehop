package com.tailormyresume.feature.onboarding.impl.pastejd

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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PasteJobDescriptionClearKeepsLabelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private lateinit var viewModel: PasteJobDescriptionViewModel

    @Before
    fun setup() {
        viewModel = PasteJobDescriptionViewModel(
            sessionRepository = session,
            nextOnboardingStep = NextOnboardingStepUseCase(session, TestProfileRepository()),
            connectivityMonitor = TestConnectivityMonitor(),
            usageAllowance = TestUsageAllowance(TestClock()),
            proposeJobLabel = ProposeJobLabelUseCase(ProposingAnalyzer()),
            discardJobDrafts = DiscardJobDraftsUseCase(TestPrepPlanRepository(), TestContentReportRepository()),
            computeDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun clear_emptiesOnlyTheJobDescriptionText() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(JD))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged("Typed Role"))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("Typed Co"))

        viewModel.onAction(PasteJobDescriptionAction.ClearTapped)

        val state = viewModel.uiState.value
        assertThat(state.text).isEmpty()
        assertThat(state.role).isEqualTo("Typed Role")
        assertThat(state.company).isEqualTo("Typed Co")
    }

    @Test
    fun clear_keepsATypedRoleAndDropsAnUntouchedCompanyPrefill() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged("Typed Role"))

        viewModel.onAction(PasteJobDescriptionAction.ClearTapped)

        val state = viewModel.uiState.value
        assertThat(state.role).isEqualTo("Typed Role")
        assertThat(state.company).isEmpty()
    }

    private class ProposingAnalyzer : JobDescriptionAnalyzer {
        override suspend fun analyze(rawText: String): JobDescription = JobDescription(
            title = "Offline Role",
            company = "Offline Co",
            rawText = rawText,
            requirements = emptyList(),
        )
    }

    private companion object {
        const val PREFILL_DEBOUNCE_MS = 300L
        val JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, and model dashboards in Power BI or Tableau. The team works in Agile with " +
            "JIRA and reports to stakeholders every Friday morning."
    }
}
