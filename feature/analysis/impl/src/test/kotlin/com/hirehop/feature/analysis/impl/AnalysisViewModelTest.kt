package com.hirehop.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactUseCase
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreateApplicationUseCase
import com.hirehop.core.domain.TailorResumeUseCase
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
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
    private var nextId = 0

    private lateinit var viewModel: AnalysisViewModel

    @Before
    fun setUp() {
        viewModel = createViewModel()
    }

    private fun createViewModel() = AnalysisViewModel(
        profileRepository = profileRepository,
        applicationRepository = applicationRepository,
        analyzeJob = AnalyzeJobUseCase(FixedJobDescriptionAnalyzer(), KeywordGapMatcher()),
        addUserStatedFact = AddUserStatedFactUseCase(profileRepository, ::newId),
        createApplication = CreateApplicationUseCase(
            applicationRepository = applicationRepository,
            tailorResume = TailorResumeUseCase(EmptyResumeTailor(), AcceptingFabricationGuard()),
            clock = FixedClock,
            idGenerator = ::newId,
        ),
        savedStateHandle = SavedStateHandle(),
        computeDispatcher = UnconfinedTestDispatcher(),
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

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    }
}
