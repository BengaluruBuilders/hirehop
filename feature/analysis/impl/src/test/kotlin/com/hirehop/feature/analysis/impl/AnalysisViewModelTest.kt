package com.hirehop.feature.analysis.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.domain.AddUserStatedFactUseCase
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreateApplicationUseCase
import com.hirehop.core.domain.TailorResumeUseCase
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.PrepPlanItem
import com.hirehop.core.model.ReportedItemKind
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.navigation.PendingNavigation
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestContentReportRepository
import com.hirehop.core.testing.repository.TestPrepPlanRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.repository.TestUsageAllowance
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestClock
import com.hirehop.feature.tailor.api.navigation.TailorNavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class AnalysisViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val sessionRepository = TestSessionRepository()
    private val profileRepository = TestProfileRepository()
    private val applicationRepository = TestApplicationRepository()
    private val flakyApplicationRepository = FlakyApplicationRepository(applicationRepository)
    private val connectivity = TestConnectivityMonitor()
    private val prepPlanRepository = TestPrepPlanRepository()
    private val contentReportRepository = TestContentReportRepository()
    private val usageAllowance = TestUsageAllowance(TestClock())
    private var paymentGateway = TestPaymentGateway()
    private val analyzer = FixedJobDescriptionAnalyzer()
    private val matcher = KeywordGapMatcher()
    private var nextId = 0

    private lateinit var viewModel: AnalysisViewModel

    @Before
    fun setUp() {
        PendingNavigation.consume()
    }

    private suspend fun TestScope.start(
        job: KeptJobDescription? = KEPT_JOB,
        scenario: DebugScenario = DebugScenario.DEFAULT,
        onboardingComplete: Boolean = false,
        profile: CandidateProfile? = confirmedProfile(),
        freeCredits: Int? = null,
    ) {
        if (freeCredits != null) paymentGateway = TestPaymentGateway().withFreeCredits(freeCredits)
        sessionRepository.sendAccount(SignInAccount.localAccount)
        sessionRepository.sendConsent(ConsentRecord(setOf(ConsentPurpose.READ_AND_BUILD), FixedClock.now(), "test"))
        sessionRepository.sendOnboardingComplete(onboardingComplete)
        profileRepository.sendProfile(profile)
        if (job != null) sessionRepository.keepJobDescription(job)
        viewModel = createViewModel()
        viewModel.onEnter(scenario)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }
    }

    private fun createViewModel() = AnalysisViewModel(
        sessionRepository = sessionRepository,
        profileRepository = profileRepository,
        nextOnboardingStep = NextOnboardingStepUseCase(sessionRepository, profileRepository),
        analyzeJob = AnalyzeJobUseCase(analyzer, matcher),
        addUserStatedFact = AddUserStatedFactUseCase(profileRepository, ::newId),
        createApplication = CreateApplicationUseCase(
            applicationRepository = flakyApplicationRepository,
            tailorResume = TailorResumeUseCase(EmptyResumeTailor(), AcceptingFabricationGuard()),
            clock = FixedClock,
            idGenerator = ::newId,
        ),
        prepPlanRepository = prepPlanRepository,
        contentReportRepository = contentReportRepository,
        usageAllowance = usageAllowance,
        paymentGateway = paymentGateway,
        clock = FixedClock,
        connectivityMonitor = connectivity,
        computeDispatcher = UnconfinedTestDispatcher(),
        applicationScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()),
    )

    private suspend fun draftPlan(): List<PrepPlanItem> =
        prepPlanRepository.observeItems(KEPT_JOB.draftKey).first()

    private fun newId(): String = "id-${nextId++}"

    private fun result() = viewModel.uiState.value as AnalysisUiState.Result

    private fun AnalysisUiState.Result.item(id: String) = requireNotNull(itemOrNull(id))

    @Test
    fun noKeptJob_leavesForPasteJobDescription() = runTest {
        start(job = null)

        viewModel.destinations.test {
            assertThat(awaitItem()).isEqualTo(AnalysisDestination.Leave(OnboardingStep.PasteJobDescription))
        }
    }

    @Test
    fun noConfirmedFact_leavesForTheNextOnboardingStep() = runTest {
        start(profile = unconfirmedProfile())

        viewModel.destinations.test {
            assertThat(awaitItem()).isEqualTo(AnalysisDestination.Leave(OnboardingStep.ConfirmFacts))
        }
    }

    @Test
    fun noProfile_leavesForImportResume() = runTest {
        start(profile = null)

        viewModel.destinations.test {
            assertThat(awaitItem()).isEqualTo(AnalysisDestination.Leave(OnboardingStep.ImportResume))
        }
    }

    @Test
    fun keptJob_runsTheAnalysisWithoutAnInputState() = runTest {
        start()

        val result = result()
        assertThat(result.keywordCoverage.covered).isEqualTo(1)
        assertThat(result.keywordCoverage.total).isEqualTo(4)
        assertThat(result.job).isEqualTo(JobLabel(title = "Associate Analyst", company = "Northwind GCC"))
    }

    @Test
    fun keptJob_withoutRoleOrCompany_usesTheAnalyzerValues() = runTest {
        start(job = KEPT_JOB.copy(role = "", company = ""))

        assertThat(result().job).isEqualTo(JobLabel(title = "Android Developer", company = "Acme"))
    }

    @Test
    fun result_groupsInTheDesignOrder() = runTest {
        start()

        assertThat(result().sections.map { it.group }).containsExactly(
            RequirementGroup.MustHaveGaps,
            RequirementGroup.Partial,
            RequirementGroup.Met,
            RequirementGroup.NiceToHaveGaps,
        ).inOrder()
    }

    @Test
    fun result_resolvesFactsAndSkillsForMetRows() = runTest {
        start()

        val result = result()
        assertThat(result.item("req-kotlin").skills).containsExactly("kotlin")
        val graphQl = result.item("req-graphql")
        assertThat(graphQl.factRefs.map { it.factId }).containsExactly("entry-1")
        assertThat(graphQl.factRefs.single().lines).containsExactly("Built dashboards backed by a GraphQL API")
        assertThat(result.item("req-sql").factRefs).isEmpty()
    }

    @Test
    fun result_showsTheFreeCredits() = runTest {
        start()

        assertThat(result().freeCredits).isEqualTo(1)
    }

    @Test
    fun scenarioLoading_showsTheWaitingState() = runTest {
        start(scenario = DebugScenario.LOADING)

        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Analyzing::class.java)
        assertThat((viewModel.uiState.value as AnalysisUiState.Analyzing).factCount).isEqualTo(2)
    }

    @Test
    fun result_countsOneDailyAnalysis() = runTest {
        start()

        assertThat(usageAllowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES - 1)
    }

    @Test
    fun jobWithoutKeyTerms_doesNotCountADailyAnalysis_andCannotTailor() = runTest {
        analyzer.withoutRequirements = true
        start()

        assertThat(result().hasKeyTerms).isFalse()
        assertThat(result().canTailor).isFalse()
        assertThat(usageAllowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
        viewModel.onTailor()
        assertThat(applicationRepository.observeApplications().first()).isEmpty()
    }

    @Test
    fun jobWithoutKeyTerms_editTheJobGoesBackToPasteJobDescription() = runTest {
        analyzer.withoutRequirements = true
        start()

        viewModel.onBackToJobDescription()

        viewModel.destinations.test {
            assertThat(awaitItem()).isEqualTo(AnalysisDestination.Leave(OnboardingStep.PasteJobDescription))
        }
        assertThat(sessionRepository.observeKeptJobDescription().first()).isEqualTo(KEPT_JOB)
    }

    @Test
    fun analyzerFailure_doesNotCountADailyAnalysis() = runTest {
        analyzer.failing = true
        start()

        assertThat(usageAllowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
    }

    @Test
    fun runningTheAnalysisAgainForTheSameJob_doesNotCountAgain() = runTest {
        start()
        profileRepository.sendProfile(confirmedProfile().let { it.copy(skills = it.skills + "SQL") })

        viewModel.onResume()
        viewModel.onRetry()

        assertThat(usageAllowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES - 1)
    }

    @Test
    fun scenarioError_showsTheErrorState_andRetryRecovers() = runTest {
        start(scenario = DebugScenario.ERROR)
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Failed::class.java)

        viewModel.onEnter(DebugScenario.DEFAULT)
        viewModel.onRetry()

        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Result::class.java)
    }

    @Test
    fun analyzerFailure_showsTheErrorState() = runTest {
        analyzer.failing = true
        start()

        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Failed::class.java)
        analyzer.failing = false
        viewModel.onRetry()
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Result::class.java)
    }

    @Test
    fun scenarioEmpty_showsTheDailyLimit_andBackGoesToThePastedJob() = runTest {
        start(scenario = DebugScenario.EMPTY)
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.DailyLimit::class.java)

        viewModel.onBackToJobDescription()

        viewModel.destinations.test {
            assertThat(awaitItem()).isEqualTo(AnalysisDestination.Leave(OnboardingStep.PasteJobDescription))
        }
    }

    @Test
    fun scenarioOffline_andRealOffline_keepTheLastResultAndBlockTailoring() = runTest {
        start(scenario = DebugScenario.OFFLINE)
        assertThat(result().isOffline).isTrue()
        assertThat(result().canTailor).isFalse()

        viewModel.onEnter(DebugScenario.DEFAULT)
        assertThat(result().isOffline).isFalse()
        connectivity.setOnline(false)
        assertThat(result().isOffline).isTrue()

        viewModel.onTailor()

        assertThat(applicationRepository.observeApplications().first()).isEmpty()
    }

    @Test
    fun scenarioPending_blocksTailoringForTheFreeLimit() = runTest {
        start(scenario = DebugScenario.PENDING)

        assertThat(result().tailorLimitReached).isTrue()
        viewModel.onTailor()
        assertThat(applicationRepository.observeApplications().first()).isEmpty()
    }

    @Test
    fun overlays_openAndDismiss() = runTest {
        start()

        viewModel.onOpenMenu("req-sql")
        assertThat(result().overlay).isEqualTo(AnalysisOverlay.Menu("req-sql"))
        viewModel.onSeeSource("req-graphql")
        assertThat(result().overlay).isEqualTo(AnalysisOverlay.Source("req-graphql"))
        viewModel.onIHaveThis("req-sql")
        assertThat(result().overlay).isEqualTo(AnalysisOverlay.Question("req-sql"))
        viewModel.onOpenShareCard()
        assertThat(result().overlay).isEqualTo(AnalysisOverlay.ShareCard)
        viewModel.onDismissOverlay()
        assertThat(result().overlay).isEqualTo(AnalysisOverlay.None)
    }

    @Test
    fun report_closesTheMenu_savesTheReport_andThanksTheUser() = runTest {
        start()
        viewModel.onOpenMenu("req-sql")

        viewModel.onReport("req-sql")

        assertThat(result().overlay).isEqualTo(AnalysisOverlay.None)
        assertThat(result().toast).isEqualTo(AnalysisToast.Reported)
        assertThat(result().item("req-sql").isReported).isTrue()
        assertThat(result().item("req-sql").hasMenu).isFalse()
        assertThat(result().item("req-kotlin").isReported).isFalse()
    }

    @Test
    fun report_followsTheApplicationOnceTailored() = runTest {
        start(onboardingComplete = true)
        viewModel.onReport("req-sql")

        viewModel.onTailor()

        val application = applicationRepository.observeApplications().first().single()
        val reports = contentReportRepository.observeReports(application.id).first()
        assertThat(reports.map { it.itemKind to it.itemId }).containsExactly(ReportedItemKind.REQUIREMENT to "req-sql")
        assertThat(reports.single().reportedAt).isEqualTo(FixedClock.now())
    }

    @Test
    fun iHaveThis_savesAUserStatedFact_andTheFractionUpdates() = runTest {
        start()
        viewModel.onIHaveThis("req-sql")

        viewModel.onSubmitEvidence("req-sql", "  I wrote SQL queries during my internship.  ")

        val result = result()
        assertThat(result.keywordCoverage.covered).isEqualTo(2)
        assertThat(result.item("req-sql").status).isEqualTo(MatchStatus.MET)
        assertThat(result.overlay).isEqualTo(AnalysisOverlay.None)
        assertThat(result.toast).isEqualTo(AnalysisToast.GapClosed)
        assertThat(result.closedRequirementId).isEqualTo("req-sql")
        val entry = requireNotNull(profileRepository.observeProfile().first()).entries.first { it.id == "U-01" }
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(entry.bullets.map { it.text }).containsExactly("I wrote SQL queries during my internship.")
    }

    @Test
    fun iHaveThis_whenTheWordsDoNotCloseTheGap_savesWithoutClaimingIt() = runTest {
        start()

        viewModel.onSubmitEvidence("req-sql", "I like tidy data.")

        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.GAP)
        assertThat(result().toast).isEqualTo(AnalysisToast.FactSaved)
        assertThat(result().closedRequirementId).isNull()
    }

    @Test
    fun iHaveThis_ignoresBlankWords() = runTest {
        start()

        viewModel.onSubmitEvidence("req-sql", "   ")

        assertThat(result().keywordCoverage.covered).isEqualTo(1)
        assertThat(result().toast).isNull()
    }

    @Test
    fun iHaveThis_whenTheSaveFails_keepsTheSheetOpen() = runTest {
        start()
        viewModel.onIHaveThis("req-sql")
        profileRepository.clearProfile()

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(result().overlay).isEqualTo(AnalysisOverlay.Question("req-sql"))
        assertThat(result().toast).isEqualTo(AnalysisToast.EvidenceFailed)
    }

    @Test
    fun undo_afterIHaveThis_restoresTheProfileAndTheFraction() = runTest {
        start()
        val before = requireNotNull(profileRepository.observeProfile().first())
        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        viewModel.onUndo()

        assertThat(profileRepository.observeProfile().first()).isEqualTo(before)
        assertThat(result().keywordCoverage.covered).isEqualTo(1)
        assertThat(result().toast).isNull()
    }

    @Test
    fun prepPlan_addWritesToTheRepositoryAtOnce_andShowsAToastWithUndo() = runTest {
        start()

        viewModel.onTogglePrepPlan("req-sql")

        assertThat(result().item("req-sql").isInPrepPlan).isTrue()
        assertThat(result().toast).isEqualTo(AnalysisToast.PrepAdded("req-sql", "SQL databases"))
        assertThat(draftPlan()).containsExactly(PrepPlanItem("req-sql", "SQL databases"))
        viewModel.onUndo()
        assertThat(result().item("req-sql").isInPrepPlan).isFalse()
        assertThat(result().toast).isNull()
        assertThat(draftPlan()).isEmpty()
    }

    @Test
    fun prepPlan_toggleOffRemovesTheRequirement() = runTest {
        start()
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onTogglePrepPlan("req-sql")

        assertThat(result().item("req-sql").isInPrepPlan).isFalse()
        assertThat(draftPlan()).isEmpty()
    }

    @Test
    fun prepPlan_dropsRequirementThatBecomesMet() = runTest {
        start()
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(result().item("req-sql").isInPrepPlan).isFalse()
        assertThat(draftPlan()).isEmpty()
    }

    @Test
    fun toast_clearsWhenTheScreenReportsItGone() = runTest {
        start()
        viewModel.onReport("req-sql")

        viewModel.onToastDismiss()

        assertThat(result().toast).isNull()
    }

    @Test
    fun tailor_duringFirstRun_setsPendingNavigation_marksOnboardingComplete_andClearsTheJob() = runTest {
        start(onboardingComplete = false)
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onTailor()

        val application = applicationRepository.observeApplications().first().single()
        assertThat(application.job.title).isEqualTo("Associate Analyst")
        assertThat(application.job.company).isEqualTo("Northwind GCC")
        assertThat(application.notes).isEmpty()
        assertThat(prepPlanRepository.observeItems(application.id).first())
            .containsExactly(PrepPlanItem("req-sql", "SQL databases"))
        assertThat(draftPlan()).isEmpty()
        assertThat(PendingNavigation.consume()).containsExactly(TailorNavKey(application.id))
        assertThat(sessionRepository.observeOnboardingComplete().first()).isTrue()
        assertThat(sessionRepository.observeKeptJobDescription().first()).isNull()
    }

    @Test
    fun tailor_afterOnboarding_opensTheTailoredResume() = runTest {
        start(onboardingComplete = true)

        viewModel.onTailor()

        val application = applicationRepository.observeApplications().first().single()
        viewModel.destinations.test {
            assertThat(awaitItem()).isEqualTo(AnalysisDestination.Tailor(application.id))
        }
        assertThat(PendingNavigation.consume()).isEmpty()
        assertThat(sessionRepository.observeKeptJobDescription().first()).isNull()
    }

    @Test
    fun tailor_whenSavingFails_keepsTheJob_andRetryDoesNotDuplicate() = runTest {
        start(onboardingComplete = true)
        flakyApplicationRepository.failOnUpsert = true

        viewModel.onTailor()

        assertThat(result().toast).isEqualTo(AnalysisToast.TailorFailed)
        assertThat(result().isTailoring).isFalse()
        assertThat(sessionRepository.observeKeptJobDescription().first()).isEqualTo(KEPT_JOB)
        flakyApplicationRepository.failOnUpsert = false
        viewModel.onTailor()
        assertThat(applicationRepository.observeApplications().first()).hasSize(1)
    }

    @Test
    fun tailor_ignoresASecondTapWhileBusy() = runTest {
        start(onboardingComplete = true)

        viewModel.onTailor()
        viewModel.onTailor()

        assertThat(applicationRepository.observeApplications().first()).hasSize(1)
    }

    @Test
    fun tailor_withACredit_doesNotCountTheFreeTailoring() = runTest {
        start(onboardingComplete = true)

        viewModel.onTailor()

        assertThat(usageAllowance.observeFreeTailoringsLeft().first()).isEqualTo(UsageAllowance.DAILY_FREE_TAILORINGS)
        assertThat(applicationRepository.observeApplications().first()).hasSize(1)
    }

    @Test
    fun tailor_withNoCredit_countsTheFreeTailoringOnce() = runTest {
        start(onboardingComplete = true, freeCredits = 0)
        assertThat(result().tailorLimitReached).isFalse()
        flakyApplicationRepository.failOnUpsert = true

        viewModel.onTailor()
        flakyApplicationRepository.failOnUpsert = false
        viewModel.onTailor()

        assertThat(usageAllowance.observeFreeTailoringsLeft().first()).isEqualTo(0)
        assertThat(applicationRepository.observeApplications().first()).hasSize(1)
    }

    @Test
    fun tailor_withNoCreditAndNoFreeTailoringLeft_showsTheLimitAndCreatesNothing() = runTest {
        usageAllowance.consumeFreeTailoring()
        start(onboardingComplete = true, freeCredits = 0)

        assertThat(result().tailorLimitReached).isTrue()
        assertThat(result().canTailor).isFalse()
        viewModel.onTailor()

        assertThat(applicationRepository.observeApplications().first()).isEmpty()
    }

    @Test
    fun tailor_withACredit_ignoresAnEmptyFreeTailoringAllowance() = runTest {
        usageAllowance.consumeFreeTailoring()
        start(onboardingComplete = true)

        assertThat(result().tailorLimitReached).isFalse()
        viewModel.onTailor()

        assertThat(applicationRepository.observeApplications().first()).hasSize(1)
    }

    @Test
    fun tailor_usesTheKeptCompanyAndRoleOverTheAnalyzer() = runTest {
        start(onboardingComplete = true, job = KEPT_JOB.copy(company = "Kept Co", role = "Kept Role"))

        viewModel.onTailor()

        val application = applicationRepository.observeApplications().first().single()
        assertThat(application.job.title).isEqualTo("Kept Role")
        assertThat(application.job.company).isEqualTo("Kept Co")
    }

    @Test
    fun result_showsTheDisplayIdOfTheSourceFact() = runTest {
        start()

        val ref = result().item("req-graphql").factRefs.single()
        assertThat(ref.factId).isEqualTo("entry-1")
        assertThat(ref.displayId).isEqualTo("P-01")
    }

    @Test
    fun resume_afterTheProfileChanged_runsTheAnalysisAgain() = runTest {
        start()
        val calls = matcher.receivedProfiles.size
        val edited = confirmedProfile().let { it.copy(skills = it.skills + "SQL") }
        profileRepository.sendProfile(edited)

        viewModel.onResume()

        assertThat(matcher.receivedProfiles.size).isGreaterThan(calls)
        assertThat(result().keywordCoverage.covered).isEqualTo(2)
    }

    @Test
    fun resume_withTheSameProfile_doesNothing() = runTest {
        start()
        val calls = matcher.receivedProfiles.size

        viewModel.onResume()

        assertThat(matcher.receivedProfiles.size).isEqualTo(calls)
    }

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }

    private companion object {
        val KEPT_JOB = KeptJobDescription(text = TEST_JOB_TEXT, company = "Northwind GCC", role = "Associate Analyst")
    }
}
