package com.hirehop.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactUseCase
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreateApplicationUseCase
import com.hirehop.core.domain.TailorResumeUseCase
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
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

    private val profileRepository = TestProfileRepository()
    private val applicationRepository = TestApplicationRepository()
    private val flakyProfileRepository = FlakyProfileRepository(profileRepository)
    private val flakyApplicationRepository = FlakyApplicationRepository(applicationRepository)
    private val analyzer = FixedJobDescriptionAnalyzer()
    private val matcher = KeywordGapMatcher()
    private var nextId = 0

    private lateinit var viewModel: AnalysisViewModel

    @Before
    fun setUp() {
        viewModel = createViewModel()
    }

    private fun createViewModel() = AnalysisViewModel(
        profileRepository = flakyProfileRepository,
        applicationRepository = flakyApplicationRepository,
        analyzeJob = AnalyzeJobUseCase(analyzer, matcher),
        addUserStatedFact = AddUserStatedFactUseCase(flakyProfileRepository, ::newId),
        createApplication = CreateApplicationUseCase(
            applicationRepository = flakyApplicationRepository,
            tailorResume = TailorResumeUseCase(EmptyResumeTailor(), AcceptingFabricationGuard()),
            clock = FixedClock,
            idGenerator = ::newId,
        ),
        savedStateHandle = SavedStateHandle(),
        computeDispatcher = UnconfinedTestDispatcher(),
        applicationScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()),
    )

    private fun newId(): String = "id-${nextId++}"

    private fun TestScope.collectUiState() =
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

    private fun resultState() = viewModel.uiState.value as AnalysisUiState.Result

    private suspend fun analyzeWithConfirmedProfile() {
        profileRepository.saveProfile(confirmedProfile())
        viewModel.onJobTextChange(TEST_JOB_TEXT)
        viewModel.onAnalyze()
    }

    @Test
    fun stateIsNoProfile_whenProfileIsMissing() = runTest {
        collectUiState()
        profileRepository.clearProfile()

        assertThat(viewModel.uiState.value).isEqualTo(AnalysisUiState.NoProfile)
    }

    @Test
    fun stateIsNoProfile_whenNoEntryIsConfirmed() = runTest {
        collectUiState()
        profileRepository.saveProfile(unconfirmedProfile())

        assertThat(viewModel.uiState.value).isEqualTo(AnalysisUiState.NoProfile)
    }

    @Test
    fun stateIsInput_withAnalyzeDisabled_whenJobTextIsShort() = runTest {
        collectUiState()
        profileRepository.saveProfile(confirmedProfile())
        viewModel.onJobTextChange("short")

        assertThat(viewModel.uiState.value)
            .isEqualTo(AnalysisUiState.Input(jobText = "short", canAnalyze = false))
    }

    @Test
    fun stateIsInput_withAnalyzeEnabled_whenJobTextIsLongEnough() = runTest {
        collectUiState()
        profileRepository.saveProfile(confirmedProfile())
        viewModel.onJobTextChange(TEST_JOB_TEXT)

        assertThat(viewModel.uiState.value)
            .isEqualTo(AnalysisUiState.Input(jobText = TEST_JOB_TEXT, canAnalyze = true))
    }

    @Test
    fun analyze_showsCoverageAndGroupsInOrder() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()

        val result = resultState()
        assertThat(result.keywordCoverage.covered).isEqualTo(1)
        assertThat(result.keywordCoverage.total).isEqualTo(4)
        assertThat(result.sections.map { it.group }).containsExactly(
            RequirementGroup.MustHaveGaps,
            RequirementGroup.Partial,
            RequirementGroup.Met,
            RequirementGroup.NiceToHaveGaps,
        ).inOrder()
        assertThat(result.title).isEqualTo("Android Developer")
        assertThat(result.company).isEqualTo("Acme")
    }

    @Test
    fun analyze_resolvesEvidenceIdsToText() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()

        val items = resultState().sections.flatMap { it.items }.associateBy { it.id }
        assertThat(items.getValue("req-kotlin").evidence).containsExactly("Skill: kotlin")
        assertThat(items.getValue("req-graphql").evidence)
            .containsExactly("Built dashboards backed by a GraphQL API")
        assertThat(items.getValue("req-sql").evidence).isEmpty()
    }

    @Test
    fun iHaveThis_addsFactAndRerunsAnalysis() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTitleChange("Edited title")

        viewModel.onSubmitEvidence("req-sql", "  I wrote SQL queries during my internship.  ")

        val result = resultState()
        assertThat(result.keywordCoverage.covered).isEqualTo(2)
        assertThat(result.title).isEqualTo("Edited title")
        val sql = result.sections.flatMap { it.items }.first { it.id == "req-sql" }
        assertThat(sql.status).isEqualTo(MatchStatus.MET)
        val bullets = requireNotNull(profileRepository.observeProfile().first())
            .entries.flatMap { it.bullets }.map { it.text }
        assertThat(bullets).contains("I wrote SQL queries during my internship.")
    }

    @Test
    fun iHaveThis_ignoresBlankStatement() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()

        viewModel.onSubmitEvidence("req-sql", "   ")

        assertThat(resultState().keywordCoverage.covered).isEqualTo(1)
    }

    @Test
    fun prepPlan_toggleAddsAndRemovesRequirement() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()

        viewModel.onTogglePrepPlan("req-sql")
        assertThat(resultState().prepPlanCount).isEqualTo(1)

        viewModel.onTogglePrepPlan("req-sql")
        assertThat(resultState().prepPlanCount).isEqualTo(0)
    }

    @Test
    fun prepPlan_dropsRequirementThatBecomesMet() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(resultState().prepPlanCount).isEqualTo(0)
    }

    @Test
    fun save_writesPrepPlanIntoApplicationNotes() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTogglePrepPlan("req-docker")
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onSave()

        val saved = viewModel.uiState.value as AnalysisUiState.Saved
        val application = requireNotNull(applicationRepository.observeApplication(saved.applicationId).first())
        assertThat(application.notes).isEqualTo("Prep: SQL databases\nPrep: Docker")
    }

    @Test
    fun save_storesEditedTitleAndCompany_andEmitsSavedState() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTitleChange("  Senior Android Developer ")
        viewModel.onCompanyChange("Globex")

        viewModel.onSave()

        val saved = viewModel.uiState.value as AnalysisUiState.Saved
        val application = requireNotNull(applicationRepository.observeApplication(saved.applicationId).first())
        assertThat(application.job.title).isEqualTo("Senior Android Developer")
        assertThat(application.job.company).isEqualTo("Globex")
        assertThat(application.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(application.notes).isEmpty()
    }

    @Test
    fun navigationConsumed_resetsToEmptyInput() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onSave()

        viewModel.onNavigationConsumed()

        assertThat(viewModel.uiState.value)
            .isEqualTo(AnalysisUiState.Input(jobText = "", canAnalyze = false))
    }

    @Test
    fun save_isRejected_whenTitleIsBlank() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTitleChange("  ")

        viewModel.onSave()

        assertThat(resultState().canSave).isFalse()
        assertThat(applicationRepository.observeApplications().first()).isEmpty()
    }

    @Test
    fun editJobText_returnsToInputWithTextKept() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()

        viewModel.onEditJobText()

        assertThat(viewModel.uiState.value)
            .isEqualTo(AnalysisUiState.Input(jobText = TEST_JOB_TEXT, canAnalyze = true))
    }

    @Test
    fun iHaveThis_storesOnlyTypedTextAsUserStatedBullet() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        val entry = requireNotNull(profileRepository.observeProfile().first())
            .entries.first { it.id == "user-stated" }
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(entry.bullets.map { it.text }).containsExactly("I wrote SQL queries during my internship.")
    }

    @Test
    fun iHaveThis_rerunsAnalysisWithUpdatedProfile() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        val callsBefore = matcher.receivedProfiles.size

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        assertThat(matcher.receivedProfiles).hasSize(callsBefore + 1)
        val latest = matcher.receivedProfiles.last()
        assertThat(latest.entries.flatMap { it.bullets }.map { it.text })
            .contains("I wrote SQL queries during my internship.")
        assertThat(latest).isEqualTo(requireNotNull(profileRepository.observeProfile().first()))
    }

    @Test
    fun analyze_passesStoredProfileToMatcher() = runTest {
        collectUiState()
        val mixed = confirmedProfile().let { profile ->
            profile.copy(entries = profile.entries + profile.entries.map { it.copy(id = "entry-2", isConfirmed = false) })
        }
        profileRepository.saveProfile(mixed)
        viewModel.onJobTextChange(TEST_JOB_TEXT)

        viewModel.onAnalyze()

        assertThat(matcher.receivedProfiles).containsExactly(mixed)
    }

    @Test
    fun analyze_returnsToInputWithError_whenAnalyzerThrows() = runTest {
        collectUiState()
        profileRepository.saveProfile(confirmedProfile())
        viewModel.onJobTextChange(TEST_JOB_TEXT)
        analyzer.failing = true

        viewModel.onAnalyze()

        assertThat(viewModel.uiState.value).isEqualTo(
            AnalysisUiState.Input(TEST_JOB_TEXT, canAnalyze = true, error = AnalysisError.AnalyzeFailed),
        )
        viewModel.onErrorShown()
        analyzer.failing = false
        viewModel.onAnalyze()
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Result::class.java)
    }

    @Test
    fun iHaveThis_keepsResultWithError_whenAddingFactFails() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        flakyProfileRepository.failOnSave = true

        viewModel.onSubmitEvidence("req-sql", "I wrote SQL queries during my internship.")

        val result = resultState()
        assertThat(result.error).isEqualTo(AnalysisError.AddEvidenceFailed)
        assertThat(result.keywordCoverage.covered).isEqualTo(1)
    }

    @Test
    fun save_returnsToResultWithError_whenStoringThrows() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        flakyApplicationRepository.failOnUpsert = true

        viewModel.onSave()

        assertThat(resultState().error).isEqualTo(AnalysisError.SaveFailed)
        assertThat(applicationRepository.observeApplications().first()).isEmpty()

        viewModel.onErrorShown()
        flakyApplicationRepository.failOnUpsert = false
        viewModel.onSave()
        assertThat(viewModel.uiState.value).isInstanceOf(AnalysisUiState.Saved::class.java)
    }

    @Test
    fun editJobText_thenAnalyzeSameText_keepsTitleCompanyAndPrepChoices() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTitleChange("Custom title")
        viewModel.onCompanyChange("Custom company")
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onEditJobText()
        viewModel.onAnalyze()

        val result = resultState()
        assertThat(result.title).isEqualTo("Custom title")
        assertThat(result.company).isEqualTo("Custom company")
        assertThat(result.prepPlanCount).isEqualTo(1)
    }

    @Test
    fun editJobText_thenAnalyzeChangedText_startsFromAnalyzerValues() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onTitleChange("Custom title")
        viewModel.onTogglePrepPlan("req-sql")

        viewModel.onEditJobText()
        viewModel.onJobTextChange("$TEST_JOB_TEXT Also needs testing skills.")
        viewModel.onAnalyze()

        val result = resultState()
        assertThat(result.title).isEqualTo("Android Developer")
        assertThat(result.prepPlanCount).isEqualTo(0)
    }

    @Test
    fun navigationConsumed_calledTwice_resetsOnlyOnce() = runTest {
        collectUiState()
        analyzeWithConfirmedProfile()
        viewModel.onSave()
        viewModel.onNavigationConsumed()
        viewModel.onJobTextChange(TEST_JOB_TEXT)

        viewModel.onNavigationConsumed()

        assertThat(viewModel.uiState.value)
            .isEqualTo(AnalysisUiState.Input(jobText = TEST_JOB_TEXT, canAnalyze = true))
    }

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }
}
