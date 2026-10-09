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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PasteJobDescriptionAnalyseSettlesLabelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private val connectivity = TestConnectivityMonitor()
    private val usage = TestUsageAllowance(TestClock())
    private val prepPlan = TestPrepPlanRepository()
    private val reports = TestContentReportRepository()
    private val analyzer = LabelAnalyzer()
    private lateinit var viewModel: PasteJobDescriptionViewModel

    @Before
    fun setup() {
        viewModel = PasteJobDescriptionViewModel(
            sessionRepository = session,
            nextOnboardingStep = NextOnboardingStepUseCase(session, TestProfileRepository()),
            connectivityMonitor = connectivity,
            usageAllowance = usage,
            proposeJobLabel = ProposeJobLabelUseCase(analyzer),
            discardJobDrafts = DiscardJobDraftsUseCase(prepPlan, reports),
            computeDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun onAction_analyseBeforeTheDebounceEnds_keepsTheLabelsFromTheText() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val kept = session.observeKeptJobDescription().first()
        assertThat(kept?.role).isEqualTo(PROPOSED_ROLE)
        assertThat(kept?.company).isEqualTo(PROPOSED_COMPANY)
    }

    @Test
    fun onAction_analyseAfterTheDebounce_keepsTheLabelsFromTheText() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val kept = session.observeKeptJobDescription().first()
        assertThat(kept?.role).isEqualTo(PROPOSED_ROLE)
        assertThat(kept?.company).isEqualTo(PROPOSED_COMPANY)
    }

    @Test
    fun onAction_companyChangedAfterTheDebounce_keepsTheTypedCompanyAndTheProposedRole() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("My own co"))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val kept = session.observeKeptJobDescription().first()
        assertThat(kept?.company).isEqualTo("My own co")
        assertThat(kept?.role).isEqualTo(PROPOSED_ROLE)
    }

    @Test
    fun onAction_withClearedLabelsBeforeTheDebounceEnds_keepsTheClearedLabels() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged(""))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged(""))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val kept = session.observeKeptJobDescription().first()
        assertThat(kept?.role).isEmpty()
        assertThat(kept?.company).isEmpty()
    }

    @Test
    fun onAction_analyseBeforeTheDebounceEnds_showsTheSettledLabelsOnScreen() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val state = viewModel.uiState.value
        assertThat(state.role).isEqualTo(PROPOSED_ROLE)
        assertThat(state.company).isEqualTo(PROPOSED_COMPANY)
    }

    private class LabelAnalyzer : JobDescriptionAnalyzer {
        override suspend fun analyze(rawText: String): JobDescription = JobDescription(
            title = PROPOSED_ROLE,
            company = PROPOSED_COMPANY,
            rawText = rawText,
            requirements = emptyList(),
        )
    }

    private companion object {
        const val PREFILL_DEBOUNCE_MS = 300L
        const val PROPOSED_ROLE = "Associate Analyst"
        const val PROPOSED_COMPANY = "Northwind GCC"

        val SAMPLE_JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, and model dashboards in Power BI or Tableau. The team works in Agile with " +
            "JIRA and reports to stakeholders every Friday morning."
    }
}
