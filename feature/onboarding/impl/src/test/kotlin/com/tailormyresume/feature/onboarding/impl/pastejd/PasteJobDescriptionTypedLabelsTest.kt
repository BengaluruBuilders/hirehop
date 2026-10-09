package com.tailormyresume.feature.onboarding.impl.pastejd

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.domain.ProposeJobLabelUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.KeptJobDescription
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

class PasteJobDescriptionTypedLabelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private val connectivity = TestConnectivityMonitor()
    private val usage = TestUsageAllowance(TestClock())
    private val prepPlan = TestPrepPlanRepository()
    private val reports = TestContentReportRepository()
    private val analyzer = BlankLabelAnalyzer()
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
    fun onAction_analyse_withTypedLabelsAndBlankPrefill_keepsTheTypedLabels() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("  Northwind  "))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged(" Analyst "))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(session.observeKeptJobDescription().first()).isEqualTo(
            KeptJobDescription(text = SAMPLE_JD, company = "Northwind", role = "Analyst"),
        )
    }

    @Test
    fun onAction_analyse_afterAddControlWithNothingTyped_keepsBlankLabels() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val kept = session.observeKeptJobDescription().first()
        assertThat(kept?.company).isEmpty()
        assertThat(kept?.role).isEmpty()
    }

    @Test
    fun onAction_analyse_withTypedLabelsBeforeTheDebounceEnds_keepsTheTypedLabels() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("Northwind"))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged("Analyst"))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        val kept = session.observeKeptJobDescription().first()
        assertThat(kept?.company).isEqualTo("Northwind")
        assertThat(kept?.role).isEqualTo("Analyst")
    }

    private class BlankLabelAnalyzer : JobDescriptionAnalyzer {
        override suspend fun analyze(rawText: String): JobDescription = JobDescription(
            title = "",
            company = "",
            rawText = rawText,
            requirements = emptyList(),
        )
    }

    private companion object {
        const val PREFILL_DEBOUNCE_MS = 300L

        val SAMPLE_JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, and model dashboards in Power BI or Tableau. The team works in Agile with " +
            "JIRA and reports to stakeholders every Friday morning."
    }
}
