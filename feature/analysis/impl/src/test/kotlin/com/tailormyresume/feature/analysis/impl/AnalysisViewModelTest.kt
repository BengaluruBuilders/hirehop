package com.tailormyresume.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.domain.AddUserStatedFactUseCase
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.AnalyzeJobUseCase
import com.tailormyresume.core.domain.CreateApplicationUseCase
import com.tailormyresume.core.domain.GapMatcher
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.JobAnalysisSource
import com.tailormyresume.core.domain.TailorResumeUseCase
import com.tailormyresume.core.domain.offline.OfflineJobAnalysisSource
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.PrepPlanItem
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.navigation.PendingNavigation
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.gateway.TestSignInGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.repository.TestUsageAllowance
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.TailorNavKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val signInGateway = TestSignInGateway(sessionRepository)
    private val analyzer = FixedJobDescriptionAnalyzer()
    private val tailor = EmptyResumeTailor()
    private val matcher = KeywordGapMatcher()
    private var viewModelMatcher: GapMatcher = matcher
    private var nextId = 0
    private var analysisCalls = 0
    private var serverOnlyMetIds = emptySet<String>()
    private var serverGapIds = emptySet<String>()
    private var serverKeywordCovered: Int? = null
    private val countingSource = object : JobAnalysisSource {
        override suspend fun analyse(profile: CandidateProfile, rawJobText: String): JobAnalysisResult {
            analysisCalls++
            val result = OfflineJobAnalysisSource(analyzer, matcher).analyse(profile, rawJobText)
            val matches = result.gap.matches.map {
                when (it.requirement.id) {
                    in serverOnlyMetIds -> it.copy(status = MatchStatus.MET)
                    in serverGapIds -> it.copy(status = MatchStatus.GAP, evidenceIds = emptyList())
                    else -> it
                }
            }
            val coverage = serverKeywordCovered
                ?.let { KeywordCoverage(covered = it, total = result.gap.keywordCoverage.total) }
                ?: result.gap.keywordCoverage
            return result.copy(gap = result.gap.copy(matches = matches, keywordCoverage = coverage))
        }
    }

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
        compute: CoroutineDispatcher = UnconfinedTestDispatcher(),
    ) {
        if (freeCredits != null) paymentGateway = TestPaymentGateway().withFreeCredits(freeCredits)
        sessionRepository.sendAccount(SignInAccount.localAccount)
        sessionRepository.sendConsent(ConsentRecord(setOf(ConsentPurpose.READ_AND_BUILD), FixedClock.now(), "test"))
        sessionRepository.sendOnboardingComplete(onboardingComplete)
        profileRepository.sendProfile(profile)
        if (job != null) sessionRepository.keepJobDescription(job)
        viewModel = createViewModel(compute)
        viewModel.onEnter(scenario)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }
    }

    private fun failed() = viewModel.uiState.value as AnalysisUiState.Failed

    private fun createViewModel(compute: CoroutineDispatcher = UnconfinedTestDispatcher()) = AnalysisViewModel(
        sessionRepository = sessionRepository,
        profileRepository = profileRepository,
        nextOnboardingStep = NextOnboardingStepUseCase(sessionRepository, profileRepository),
        analyzeJob = AnalyzeJobUseCase(countingSource),
        gapMatcher = viewModelMatcher,
        addUserStatedFact = AddUserStatedFactUseCase(profileRepository, ::newId),
        createApplication = CreateApplicationUseCase(
            applicationRepository = flakyApplicationRepository,
            tailorResume = TailorResumeUseCase(tailor, AcceptingFabricationGuard()),
            clock = FixedClock,
            idGenerator = ::newId,
        ),
        prepPlanRepository = prepPlanRepository,
        contentReportRepository = contentReportRepository,
        usageAllowance = usageAllowance,
        paymentGateway = paymentGateway,
        signInGateway = signInGateway,
        clock = FixedClock,
        idGenerator = ::newId,
        savedState = SavedStateHandle(),
        connectivityMonitor = connectivity,
        computeDispatcher = compute,
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

        assertThat(result().totalCredits).isEqualTo(1)
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
    fun serverAllowanceExhausted_showsTheDailyLimit_andCountsNothing() = runTest {
        analyzer.failure = AiFailure.AllowanceExhausted
        start()

        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.DailyLimit::class.java)
        assertThat(usageAllowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
    }

    @Test
    fun otherServerFailures_showTheErrorState_andRetryRecovers() = runTest {
        AiFailure.entries.filter { it != AiFailure.AllowanceExhausted }.forEach { failure ->
            analyzer.failure = failure
            start()
            assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Failed::class.java)
            analyzer.failure = null
            viewModel.onRetry()
            assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Result::class.java)
        }
    }

    @Test
    fun rateLimited_showsTheWaitState_namingTheWait_andRetryRecovers() = runTest {
        analyzer.failure = AiFailure.RateLimited
        analyzer.retryAfterSeconds = 30
        start()

        assertThat(failed().cause).isEqualTo(FailureCause.RateLimited(30))
        analyzer.failure = null
        viewModel.onRetry()
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Result::class.java)
    }

    @Test
    fun analysisInProgress_showsTheStillAnalysingState_andDoesNotRetryByItself() = runTest {
        analyzer.failure = AiFailure.AnalysisInProgress
        start()
        advanceUntilIdle()

        assertThat(failed().cause).isEqualTo(FailureCause.InProgress)
        assertThat(analysisCalls).isEqualTo(1)
        analyzer.failure = null
        viewModel.onRetry()
        assertThat(analysisCalls).isEqualTo(2)
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Result::class.java)
    }

    @Test
    fun quotaExceeded_showsTheDailyAiLimitState() = runTest {
        analyzer.failure = AiFailure.QuotaExceeded
        start()

        assertThat(failed().cause).isEqualTo(FailureCause.QuotaReached)
        assertThat(usageAllowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
    }

    @Test
    fun signInRequired_showsTheSignInAgainState_andUsesTheReSignInPath() = runTest {
        analyzer.failure = AiFailure.SignInRequired
        start()

        assertThat(failed().cause).isEqualTo(FailureCause.SignInRequired)
        viewModel.onSignInAgain()
        assertThat(sessionRepository.observeAccount().first()).isNull()
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
    fun iHaveThis_rematchesOnTheDevice_andDoesNotAnalyseAgain() = runTest {
        start()
        val callsBefore = analysisCalls
        val idsBefore = result().items.map { it.id }

        viewModel.onSubmitEvidence("req-sql", "  I wrote SQL queries during my internship.  ")

        val result = result()
        assertThat(analysisCalls).isEqualTo(callsBefore)
        assertThat(result.items.map { it.id }).containsExactlyElementsIn(idsBefore)
        assertThat(result.item("req-sql").status).isEqualTo(MatchStatus.MET)
        assertThat(result.closedRequirementId).isEqualTo("req-sql")
    }

    @Test
    fun iHaveThis_neverDowngradesAMatchTheServerFound() = runTest {
        serverOnlyMetIds = setOf("req-docker")
        start()

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(result().item("req-docker").status).isEqualTo(MatchStatus.MET)
        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun iHaveThis_whenTheWordsCloseTheGapWithoutNamingTheKeyword_stillSaves() = runTest {
        start()

        viewModel.onSubmitEvidence("req-sql", "My work used SQLite daily.")

        val result = result()
        assertThat(result.item("req-sql").status).isNotEqualTo(MatchStatus.GAP)
        assertThat(result.toast).isEqualTo(AnalysisToast.GapClosed)
        val entry = requireNotNull(profileRepository.observeProfile().first()).entries.first { it.id == "U-01" }
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(entry.bullets.map { it.text }).contains("My work used SQLite daily.")
    }

    @Test
    fun iHaveThis_ignoresASecondSubmitOfTheSameWords() = runTest {
        start(compute = StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        val before = requireNotNull(profileRepository.observeProfile().first())
        val bulletsBefore = before.entries.sumOf { it.bullets.size }

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")
        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")
        advanceUntilIdle()

        val profile = requireNotNull(profileRepository.observeProfile().first())
        val entry = profile.entries.first { it.id == "U-01" }
        assertThat(entry.bullets).hasSize(1)
        assertThat(profile.entries.sumOf { it.bullets.size }).isEqualTo(bulletsBefore + 1)
    }

    @Test
    fun iHaveThis_whenTheWordsDoNotCloseTheGap_savesNothingAndExplainsWhy() = runTest {
        start()
        val before = profileRepository.observeProfile().first()

        viewModel.onIHaveThis("req-sql")
        viewModel.onSubmitEvidence("req-sql", "I like tidy data.")

        val result = result()
        assertThat(result.item("req-sql").status).isEqualTo(MatchStatus.GAP)
        assertThat(result.toast).isNull()
        assertThat(result.closedRequirementId).isNull()
        assertThat(result.overlay).isEqualTo(AnalysisOverlay.Question("req-sql", notClosed = true))
        assertThat(profileRepository.observeProfile().first()).isEqualTo(before)
    }

    @Test
    fun iHaveThis_whenTheWordsDoNotCloseTheGap_attachesTheWordsToNoRow() = runTest {
        start()
        val before = requireNotNull(profileRepository.observeProfile().first())
        val bulletsBefore = before.entries.sumOf { it.bullets.size }

        viewModel.onSubmitEvidence("req-sql", "I like tidy data.")

        val profile = requireNotNull(profileRepository.observeProfile().first())
        assertThat(profile.entries).hasSize(before.entries.size)
        assertThat(profile.entries.sumOf { it.bullets.size }).isEqualTo(bulletsBefore)
        assertThat(profile.entries.map { it.source }).doesNotContain(FactSource.USER_STATED)
        val rows = result().sections.flatMap { it.items }
        assertThat(rows.flatMap { item -> item.factRefs.filter { it.source == FactSource.USER_STATED } }).isEmpty()
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
        assertThat(flakyApplicationRepository.attemptedIds.distinct()).hasSize(1)
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
    fun tailor_whenTheServerHasNoCredit_showsTheLimitAndNoFailureToast() = runTest {
        start(onboardingComplete = true)
        tailor.failure = AiFailure.NoCredit

        viewModel.onTailor()

        assertThat(result().tailorLimitReached).isTrue()
        assertThat(result().isTailoring).isFalse()
        assertThat(result().toast).isNull()
    }

    @Test
    fun tailor_whenTheServerFailsOtherwise_showsTheFailureToast() = runTest {
        start(onboardingComplete = true)
        tailor.failure = AiFailure.Unavailable

        viewModel.onTailor()

        assertThat(result().toast).isEqualTo(AnalysisToast.TailorFailed)
        assertThat(result().isTailoring).isFalse()
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

    @Test
    fun iHaveThis_whenServerSaysGapButDeviceAlreadyMatches_keepsTheOtherRequirementAndItsPrepItem() = runTest {
        serverGapIds = setOf("req-graphql")
        start()
        viewModel.onTogglePrepPlan("req-graphql")

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(result().item("req-graphql").status).isEqualTo(MatchStatus.GAP)
        assertThat(draftPlan().map { it.id }).contains("req-graphql")
    }

    @Test
    fun iHaveThis_whenEvidenceClosesReqSql_upgradesOnlyReqSqlAndDropsOnlyItsPrepItem() = runTest {
        serverGapIds = setOf("req-graphql")
        start()
        viewModel.onTogglePrepPlan("req-graphql")
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.MET)
        assertThat(result().toast).isEqualTo(AnalysisToast.GapClosed)
        assertThat(result().item("req-graphql").status).isEqualTo(MatchStatus.GAP)
        assertThat(draftPlan().map { it.id }).containsExactly("req-graphql")
    }

    @Test
    fun iHaveThis_neverDowngradesBelowTheServerStatus_andUpgradesOnlyChangedRequirements() = runTest {
        serverOnlyMetIds = setOf("req-docker")
        serverGapIds = setOf("req-graphql")
        start()

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(result().item("req-docker").status).isEqualTo(MatchStatus.MET)
        assertThat(result().item("req-graphql").status).isEqualTo(MatchStatus.GAP)
        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.MET)
    }

    @Test
    fun iHaveThis_newEvidenceClosesTheTargetedRequirementAndOthersStay() = runTest {
        serverGapIds = setOf("req-docker", "req-kotlin")
        start(profile = confirmedProfile().let { it.copy(skills = listOf("Kotlin")) })

        viewModel.onSubmitEvidence("req-docker", "I shipped Docker images during my internship.")

        val result = result()
        assertThat(result.item("req-docker").status).isEqualTo(MatchStatus.MET)
        assertThat(result.item("req-kotlin").status).isEqualTo(MatchStatus.GAP)
        assertThat(result.toast).isEqualTo(AnalysisToast.GapClosed)
        assertThat(result.closedRequirementId).isEqualTo("req-docker")
        val entry = requireNotNull(profileRepository.observeProfile().first()).entries.first { it.id == "U-01" }
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(entry.bullets.map { it.text }).containsExactly("I shipped Docker images during my internship.")
    }

    @Test
    fun iHaveThis_keywordCoverageGrowsOnlyByTheKeywordsTheNewFactAdds() = runTest {
        serverKeywordCovered = 0
        start(profile = confirmedProfile().let { it.copy(skills = listOf("Kotlin", "Docker")) })
        assertThat(result().keywordCoverage.covered).isEqualTo(0)

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        val result = result()
        assertThat(result.keywordCoverage.covered).isEqualTo(1)
        assertThat(result.keywordCoverage.total).isEqualTo(4)
    }

    @Test
    fun undo_afterIHaveThis_restoresServerMatchesAndKeepsOtherPrepItems() = runTest {
        serverGapIds = setOf("req-graphql")
        start()
        viewModel.onTogglePrepPlan("req-graphql")
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")
        viewModel.onUndo()

        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.GAP)
        assertThat(result().item("req-graphql").status).isEqualTo(MatchStatus.GAP)
        assertThat(draftPlan().map { it.id }).containsExactly("req-graphql")
        assertThat(result().toast).isNull()
    }

    @Test
    fun iHaveThis_laterSaveKeepsEarlierEvidenceClosedRequirementMet_andUndoRestoresOnlyTheLastOne() = runTest {
        serverGapIds = setOf("req-docker", "req-sql")
        start(profile = confirmedProfile().let { it.copy(skills = listOf("Kotlin")) })
        viewModel.onTogglePrepPlan("req-docker")
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onSubmitEvidence("req-docker", "I shipped Docker images during my internship.")
        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        val closed = result()
        assertThat(closed.item("req-docker").status).isEqualTo(MatchStatus.MET)
        assertThat(closed.item("req-sql").status).isEqualTo(MatchStatus.MET)
        assertThat(draftPlan().map { it.id }).containsNoneOf("req-docker", "req-sql")

        viewModel.onUndo()

        val undone = result()
        assertThat(undone.item("req-docker").status).isEqualTo(MatchStatus.MET)
        assertThat(undone.item("req-sql").status).isEqualTo(MatchStatus.GAP)
        assertThat(draftPlan().map { it.id }).doesNotContain("req-docker")
    }

    @Test
    fun tailor_afterEvidenceForOneRequirement_sendsOnlyThatUpgradeToCreateApplication() = runTest {
        serverGapIds = setOf("req-graphql")
        start(onboardingComplete = true)

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")
        viewModel.onTailor()

        val application = applicationRepository.observeApplications().first().single()
        val statuses = requireNotNull(application.gapAnalysis).matches
            .associate { it.requirement.id to it.status }
        assertThat(statuses["req-sql"]).isEqualTo(MatchStatus.MET)
        assertThat(statuses["req-graphql"]).isEqualTo(MatchStatus.GAP)
    }

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }

    private companion object {
        val KEPT_JOB = KeptJobDescription(text = TEST_JOB_TEXT, company = "Northwind GCC", role = "Associate Analyst")
    }

    @Test
    fun iHaveThis_anUnrelatedRequirementUpgradesOnlyWhenItsEvidenceCitesTheNewFact() = runTest {
        serverGapIds = setOf("req-sql", "req-docker", "req-graphql")
        start()

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries and shipped Docker images during my internship.")

        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.MET)
        assertThat(result().item("req-docker").status).isEqualTo(MatchStatus.PARTIAL)
        assertThat(result().item("req-graphql").status).isEqualTo(MatchStatus.GAP)
    }

    @Test
    fun iHaveThis_aStatementThatAddsNothingMatchable_closesNothingEvenWhenTheDeviceAlreadyMatches() = runTest {
        serverGapIds = setOf("req-docker")
        start(profile = confirmedProfile().let { it.copy(skills = listOf("Kotlin", "Docker")) })
        val before = profileRepository.observeProfile().first()

        viewModel.onSubmitEvidence("req-docker", "I like tidy data.")

        val result = result()
        assertThat(result.item("req-docker").status).isEqualTo(MatchStatus.GAP)
        assertThat(result.overlay).isEqualTo(AnalysisOverlay.Question("req-docker", notClosed = true))
        assertThat(result.toast).isNull()
        assertThat(profileRepository.observeProfile().first()).isEqualTo(before)
    }

    @Test
    fun iHaveThis_aMatcherCitingEverySupportingBulletUpgradesTheOtherRequirementOnNewEvidence() = runTest {
        serverGapIds = setOf("req-sql", "req-graphql")
        viewModelMatcher = CitingEveryBulletMatcher()
        start()

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL and GraphQL queries.")

        assertThat(result().item("req-sql").status).isEqualTo(MatchStatus.PARTIAL)
        assertThat(result().item("req-graphql").status).isEqualTo(MatchStatus.PARTIAL)
    }

    @Test
    fun result_carriesTheIdTheNextUserStatedFactWillGet() = runTest {
        val userStated = ProfileEntry(
            id = "U-03",
            category = EntryCategory.ACHIEVEMENT,
            title = "Additional experience",
            organization = "",
            startDate = "",
            endDate = "",
            bullets = listOf(EvidenceBullet("bullet-u3", "I led a college club.")),
            source = FactSource.USER_STATED,
            isConfirmed = true,
        )
        start(profile = confirmedProfile().let { it.copy(entries = it.entries + userStated) })

        assertThat(result().nextFactId).isEqualTo("U-03")
    }

    @Test
    fun result_carriesTheTimeOfTheAnalysis() = runTest {
        start()

        assertThat(result().analysedAt).isEqualTo(FixedClock.now())
    }
}
