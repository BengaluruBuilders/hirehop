package com.tailormyresume.feature.onboarding.impl.pastejd

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.domain.ProposeJobLabelUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.PrepPlanItem
import com.tailormyresume.core.model.SignInAccount
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

class PasteJobDescriptionViewModelTest {

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
    fun onEnter_default_startsEmptyWithTheActionOff() {
        val state = viewModel.uiState.value
        assertThat(state.text).isEmpty()
        assertThat(state.wordCount).isEqualTo(0)
        assertThat(state.canAnalyse).isFalse()
        assertThat(state.canClear).isFalse()
    }

    @Test
    fun onEnter_loading_namesTheRealStep() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.LOADING))

        assertThat(viewModel.uiState.first().isLoading).isTrue()
    }

    @Test
    fun onEnter_empty_saysTheShareHadNoText() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.EMPTY))

        val state = viewModel.uiState.first()
        assertThat(state.text).isEmpty()
        assertThat(state.message).isEqualTo(PasteJobDescriptionMessage.NOTHING_TO_READ)
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onEnter_offline_flagsOfflineAndKeepsTheTextEditable() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.OFFLINE))

        val state = viewModel.uiState.first()
        assertThat(state.isOffline).isTrue()
        assertThat(state.message).isNull()
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onEnter_error_reportsAFailedPaste() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        assertThat(viewModel.uiState.first().message)
            .isEqualTo(PasteJobDescriptionMessage.PASTE_FAILED)
    }

    @Test
    fun onAction_retry_clearsTheFailedPaste() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(PasteJobDescriptionAction.RetryTapped)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun onEnter_partial_saysOnlyPartOfTheShareCameThrough() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.PARTIAL))

        assertThat(viewModel.uiState.first().message)
            .isEqualTo(PasteJobDescriptionMessage.PASTE_PARTIAL)
    }

    @Test
    fun onEnter_success_readsAsAReadyScreenWithNoMessage() = runTest {
        viewModel.onEnter(
            PasteJobDescriptionNavKey(scenario = DebugScenario.SUCCESS),
            sharedText = SAMPLE_JD,
        )

        val state = viewModel.uiState.first()
        assertThat(state.message).isNull()
        assertThat(state.arrival).isEqualTo(PasteJobDescriptionArrival.SHARED_IN)
        assertThat(state.canAnalyse).isTrue()
    }

    @Test
    fun onEnter_withSharedText_prefillsAndMarksTheSharedArrival() = runTest {
        viewModel.onEnter(
            PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT),
            sharedText = SAMPLE_JD,
        )

        val state = viewModel.uiState.first()
        assertThat(state.text).isEqualTo(SAMPLE_JD)
        assertThat(state.arrival).isEqualTo(PasteJobDescriptionArrival.SHARED_IN)
        assertThat(state.canAnalyse).isTrue()
    }

    @Test
    fun onEnter_withOnlyWhitespace_sharedText_staysEmpty() = runTest {
        viewModel.onEnter(
            PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT),
            sharedText = "   \n  ",
        )

        val state = viewModel.uiState.first()
        assertThat(state.text).isEmpty()
        assertThat(state.arrival).isEqualTo(PasteJobDescriptionArrival.TYPED)
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.OFFLINE))
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun onAction_textChanged_countsWordsAsYouType() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate Analyst"))
        assertThat(viewModel.uiState.value.wordCount).isEqualTo(2)

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate  Analyst\n"))
        assertThat(viewModel.uiState.value.wordCount).isEqualTo(2)
    }

    @Test
    fun onAction_textChanged_keepsTheActionOffUntilThereIsSomethingToRead() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate"))
        assertThat(viewModel.uiState.value.canAnalyse).isFalse()
        assertThat(viewModel.uiState.value.problem)
            .isEqualTo(PasteJobDescriptionProblem.TOO_SHORT)

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        assertThat(viewModel.uiState.value.canAnalyse).isTrue()
    }

    @Test
    fun onAction_textChanged_keepsTheActionOffForABareLink() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("https://jobs.example.com/role/1"))

        val state = viewModel.uiState.value
        assertThat(state.problem).isEqualTo(PasteJobDescriptionProblem.LINK_ONLY)
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onAction_textChanged_keepsTheActionOffWhenTheTextRunsOverTheCap() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("word ".repeat(PASTE_JD_MAX_CHARACTERS)))

        val state = viewModel.uiState.value
        assertThat(state.problem).isEqualTo(PasteJobDescriptionProblem.TOO_LONG)
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onAction_textChanged_clearsAnEarlierMessage() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun onAction_clear_emptiesTheDraftWhenThereIsSomethingToClear() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("Northwind GCC"))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged("Associate Analyst"))

        viewModel.onAction(PasteJobDescriptionAction.ClearTapped)

        val state = viewModel.uiState.value
        assertThat(state.text).isEmpty()
        assertThat(state.company).isEmpty()
        assertThat(state.role).isEmpty()
        assertThat(state.wordCount).isEqualTo(0)
        assertThat(state.canClear).isFalse()
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onAction_clear_doesNothingWhenTheDraftIsAlreadyEmpty() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.EMPTY))

        viewModel.onAction(PasteJobDescriptionAction.ClearTapped)

        assertThat(viewModel.uiState.value.message)
            .isEqualTo(PasteJobDescriptionMessage.NOTHING_TO_READ)
    }

    @Test
    fun onAction_analyse_keepsTheTrimmedDraftInTheSession() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged("\n$SAMPLE_JD\n"))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged(" Northwind GCC "))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged(" Associate Analyst "))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(session.observeKeptJobDescription().first()).isEqualTo(
            KeptJobDescription(text = SAMPLE_JD, company = "Northwind GCC", role = "Associate Analyst"),
        )
    }

    @Test
    fun onAction_analyse_whenSignedOut_asksForSignIn() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.nextStep).isEqualTo(OnboardingStep.SignIn)
    }

    @Test
    fun onAction_analyse_whenSignedInWithoutConsent_asksForConsent() = runTest {
        session.sendAccount(SignInAccount.localAccount)
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.nextStep).isEqualTo(OnboardingStep.Consent)
    }

    @Test
    fun onAction_analyse_doesNothingWhileTheTextIsTooShort() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate Analyst role"))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.nextStep).isNull()
        assertThat(session.observeKeptJobDescription().first()).isNull()
    }

    @Test
    fun onAction_analyse_doesNothingWhileLoading() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.LOADING))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun onAction_analyse_doesNothingWhenTheDailyLimitIsReached() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.PENDING))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.isDailyLimitReached).isTrue()
        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun onAction_textChanged_waitsForTheDebounceBeforeItPrefills() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS - 1)

        assertThat(viewModel.uiState.value.company).isEmpty()
        advanceTimeBy(2)
        assertThat(viewModel.uiState.value.company).isEqualTo(PROPOSED_COMPANY)
    }

    @Test
    fun onAction_textChanged_whenTheServerFails_leavesTheLabelsEmptyAndKeepsTheText() = runTest {
        analyzer.failure = AiFailure.Network
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)

        assertThat(viewModel.uiState.value.text).isEqualTo(SAMPLE_JD)
        assertThat(viewModel.uiState.value.company).isEmpty()
        assertThat(viewModel.uiState.value.role).isEmpty()
    }

    @Test
    fun onAction_textChanged_prefillsCompanyAndRoleFromTheText() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)

        assertThat(viewModel.uiState.value.company).isEqualTo(PROPOSED_COMPANY)
        assertThat(viewModel.uiState.value.role).isEqualTo(PROPOSED_ROLE)
    }

    @Test
    fun onAction_textChanged_keepsWhatThePersonTyped() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("My own company"))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("$SAMPLE_JD More words."))
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)

        assertThat(viewModel.uiState.value.company).isEqualTo("My own company")
        assertThat(viewModel.uiState.value.role).isEqualTo(PROPOSED_ROLE)
    }

    @Test
    fun onEnter_withSharedText_prefillsCompanyAndRole() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT), sharedText = SAMPLE_JD)
        advanceTimeBy(PREFILL_DEBOUNCE_MS + 1)

        assertThat(viewModel.uiState.value.company).isEqualTo(PROPOSED_COMPANY)
        assertThat(viewModel.uiState.value.role).isEqualTo(PROPOSED_ROLE)
    }

    @Test
    fun onAction_analyse_withBlankCompanyAndRole_stillKeepsTheJob() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged(""))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged(""))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(session.observeKeptJobDescription().first())
            .isEqualTo(KeptJobDescription(text = SAMPLE_JD, company = "", role = ""))
    }

    @Test
    fun onAction_analyse_doesNotCountADailyAnalysis() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(usage.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
        assertThat(viewModel.uiState.value.freeAnalysesLeft).isEqualTo(UsageAllowance.DAILY_ANALYSES)
    }

    @Test
    fun onAction_analyse_withOneAnalysisLeft_keepsTheJob() = runTest {
        repeat(UsageAllowance.DAILY_ANALYSES - 1) { usage.consumeAnalysis() }
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(session.observeKeptJobDescription().first()).isNotNull()
    }

    @Test
    fun onAction_analyse_withADifferentJob_discardsTheDraftsOfTheOldJob() = runTest {
        val old = KeptJobDescription(text = "Old job text", company = "", role = "")
        session.keepJobDescription(old)
        prepPlan.add(old.draftKey, PrepPlanItem("req-1", "Docker"))
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(prepPlan.observeItems(old.draftKey).first()).isEmpty()
    }

    @Test
    fun onAction_analyse_withTheSameJob_keepsItsDrafts() = runTest {
        val same = KeptJobDescription(text = SAMPLE_JD, company = "", role = "")
        session.keepJobDescription(same)
        prepPlan.add(same.draftKey, PrepPlanItem("req-1", "Docker"))
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(prepPlan.observeItems(same.draftKey).first()).hasSize(1)
    }

    @Test
    fun onAction_analyse_whenNoAnalysisIsLeft_showsTheLimitAndKeepsNothing() = runTest {
        repeat(UsageAllowance.DAILY_ANALYSES) { usage.consumeAnalysis() }
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.isDailyLimitReached).isTrue()
        assertThat(viewModel.uiState.value.canAnalyse).isFalse()
        assertThat(viewModel.uiState.value.nextStep).isNull()
        assertThat(session.observeKeptJobDescription().first()).isNull()
    }

    @Test
    fun onAction_nextStepConsumed_clearsTheStep() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        viewModel.onAction(PasteJobDescriptionAction.NextStepConsumed)

        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun onAction_pasted_withNoText_saysThereWasNothingToPaste() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.Pasted("  "))

        assertThat(viewModel.uiState.value.message).isEqualTo(PasteJobDescriptionMessage.NOTHING_TO_READ)
    }

    @Test
    fun onAction_pasted_withText_fillsTheDraft() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.Pasted(SAMPLE_JD))

        assertThat(viewModel.uiState.value.text).isEqualTo(SAMPLE_JD)
    }

    @Test
    fun onEnter_whenTheDeviceGoesOffline_flagsOffline() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun onAction_dismissMessage_clearsTheMessage() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(PasteJobDescriptionAction.DismissMessageTapped)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun pasteJdWordCount_ignoresRunsOfWhitespace() {
        assertThat(pasteJdWordCount("")).isEqualTo(0)
        assertThat(pasteJdWordCount("   \n\t ")).isEqualTo(0)
        assertThat(pasteJdWordCount("one two   three\nfour")).isEqualTo(4)
    }

    @Test
    fun pasteJdProblem_isNothingForAnEmptyDraft() {
        assertThat(pasteJdProblem("")).isNull()
        assertThat(pasteJdProblem("   ")).isNull()
    }

    @Test
    fun pasteJdProblem_flagsAJdInsideALongSentenceAsRead() {
        val longSentence = "SQL and Power BI are required for this role in Bengaluru across three teams that build weekly finance dashboards for our stores"

        assertThat(pasteJdProblem(longSentence)).isNull()
    }

    private class LabelAnalyzer : JobDescriptionAnalyzer {
        var failure: AiFailure? = null

        override suspend fun analyze(rawText: String): JobDescription {
            failure?.let { throw AiException(it) }
            return JobDescription(
                title = PROPOSED_ROLE,
                company = PROPOSED_COMPANY,
                rawText = rawText,
                requirements = emptyList(),
            )
        }
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
