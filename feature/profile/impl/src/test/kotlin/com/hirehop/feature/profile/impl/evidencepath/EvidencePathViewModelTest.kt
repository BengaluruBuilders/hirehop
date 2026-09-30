package com.hirehop.feature.profile.impl.evidencepath

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.profile.api.navigation.FactEvidenceNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EvidencePathViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private var nextBulletId = 0

    private val idGenerator = IdGenerator { "bullet-${nextBulletId++}" }

    private lateinit var viewModel: EvidencePathViewModel

    @Before
    fun setup() {
        repository.sendProfile(blankProfile())
        viewModel = EvidencePathViewModel(
            addUserStatedFacts = AddUserStatedFactsUseCase(
                profileRepository = repository,
                idGenerator = idGenerator,
            ),
        )
    }

    @Test
    fun onEnter_startsOnTheFirstQuestionOfTheCarriedCategory() {
        viewModel.onEnter(FactEvidenceNavKey())

        val state = viewModel.uiState.value
        assertThat(state.startCategory).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(state.category).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(state.question?.prompt).isEqualTo(EvidencePrompt.TITLE)
    }

    @Test
    fun categoryChosen_opensTheFirstQuestionOfThatCategory() {
        viewModel.onEnter(FactEvidenceNavKey())

        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.COMPETITIONS))

        val question = viewModel.uiState.value.question
        assertThat(question?.category).isEqualTo(EvidenceCategory.COMPETITIONS)
        assertThat(question?.prompt).isEqualTo(EvidencePrompt.TITLE)
        assertThat(question?.totalPrompts).isEqualTo(2)
    }

    @Test
    fun categoryChosen_forInternships_asksForTheCompanyToo() {
        viewModel.onEnter(FactEvidenceNavKey())

        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.INTERNSHIPS))

        assertThat(viewModel.uiState.value.question?.totalPrompts).isEqualTo(3)
    }

    @Test
    fun categoryChosen_keepsTheFactsAlreadyFolded() = runTest {
        answerProjectsAndSave()

        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.COURSEWORK))

        assertThat(viewModel.uiState.value.cards).hasSize(1)
    }

    @Test
    fun answerChanged_keepsOnlyWhatTheUserTyped() {
        viewModel.onEnter(FactEvidenceNavKey())

        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Placement Stats Dashboard"))

        assertThat(viewModel.uiState.value.question?.title).isEqualTo("Placement Stats Dashboard")
    }

    @Test
    fun nextPrompt_movesThroughTheQuestionsOfTheCategory() {
        chooseProjects()

        viewModel.onAction(EvidencePathAction.NextPrompt)
        assertThat(viewModel.uiState.value.question?.prompt).isEqualTo(EvidencePrompt.DETAIL)

        viewModel.onAction(EvidencePathAction.NextPrompt)
        assertThat(viewModel.uiState.value.question?.isLastPrompt).isTrue()
    }

    @Test
    fun nextPrompt_onTheLastQuestion_doesNothing() {
        chooseProjects()
        viewModel.onAction(EvidencePathAction.NextPrompt)

        viewModel.onAction(EvidencePathAction.NextPrompt)

        assertThat(viewModel.uiState.value.question?.promptIndex).isEqualTo(1)
    }

    @Test
    fun backPrompt_goesBackOneQuestion() {
        chooseProjects()
        viewModel.onAction(EvidencePathAction.NextPrompt)

        viewModel.onAction(EvidencePathAction.BackPrompt)

        assertThat(viewModel.uiState.value.question?.promptIndex).isEqualTo(0)
    }

    @Test
    fun backPrompt_onTheFirstQuestion_returnsToThePicker() {
        chooseProjects()

        viewModel.onAction(EvidencePathAction.BackPrompt)

        val state = viewModel.uiState.value
        assertThat(state.isPicker).isTrue()
        assertThat(state.question).isNull()
    }

    @Test
    fun skipPrompt_movesOnWithoutAnAnswer() {
        chooseProjects()

        viewModel.onAction(EvidencePathAction.SkipPrompt)

        val question = viewModel.uiState.value.question
        assertThat(question?.promptIndex).isEqualTo(1)
        assertThat(question?.title).isEmpty()
    }

    @Test
    fun skipPrompt_onTheLastQuestion_recordsTheCategoryAsSkipped() {
        chooseProjects()
        viewModel.onAction(EvidencePathAction.NextPrompt)

        viewModel.onAction(EvidencePathAction.SkipPrompt)

        val state = viewModel.uiState.value
        assertThat(state.skipped).containsExactly(EvidenceCategory.PROJECTS)
        assertThat(state.category).isEqualTo(EvidenceCategory.INTERNSHIPS)
    }

    @Test
    fun skipCategory_leavesTheQuestionAndMovesOn() {
        chooseProjects()

        viewModel.onAction(EvidencePathAction.SkipCategory)

        val state = viewModel.uiState.value
        assertThat(state.skipped).containsExactly(EvidenceCategory.PROJECTS)
        assertThat(state.cards).isEmpty()
    }

    @Test
    fun save_whenTheTitleIsBlank_showsTheProblemOnThatQuestion() {
        chooseProjects()
        viewModel.onAction(EvidencePathAction.NextPrompt)

        viewModel.onAction(EvidencePathAction.Save)

        val state = viewModel.uiState.value
        assertThat(state.isSaveRejected).isTrue()
        assertThat(state.message).isEqualTo(EvidenceMessage.SAVE_REJECTED)
        assertThat(state.question?.problems)
            .containsEntry(EvidencePrompt.TITLE, EvidenceFieldProblem.REQUIRED)
    }

    @Test
    fun skipCategory_onTheLastCategory_showsTheSummaryWithTheSkipCounted() = runTest {
        viewModel.onEnter(FactEvidenceNavKey())
        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.POSITIONS))

        viewModel.onAction(EvidencePathAction.SkipCategory)

        assertThat(viewModel.uiState.value.done)
            .isEqualTo(EvidenceDone(addedCount = 0, skippedCount = 1))
    }

    @Test
    fun save_withACompleteAnswer_foldsAFactCardStampedUserStated() = runTest {
        answerProjectsAndSave()

        val card = viewModel.uiState.value.cards.single()
        assertThat(card.category).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(card.entryCategory).isEqualTo(EntryCategory.PROJECT)
        assertThat(card.line).contains("Placement Stats Dashboard")
    }

    @Test
    fun save_putsTheFactInTheProfileAsUserStated() = runTest {
        answerProjectsAndSave()

        val saved = checkNotNull(repository.observeProfile().first()) { "Expected a saved profile" }
        val added = saved.entries.single()
        assertThat(added.title).isEqualTo("Placement Stats Dashboard")
        assertThat(added.organization).isEmpty()
        assertThat(added.bullets.single().text)
            .isEqualTo("Power BI and Excel. The T and P cell used it for the 2024 placement report.")
        assertThat(added.source).isEqualTo(FactSource.USER_STATED)
        assertThat(added.isConfirmed).isFalse()
    }

    @Test
    fun save_givesTheFoldedCardTheRealFactId() = runTest {
        answerProjectsAndSave()

        val entry = viewModel.uiState.value.cards.single().entry
        assertThat(entry).isNotNull()
        assertThat(entry?.id).isNotEmpty()
    }

    @Test
    fun save_movesToTheNextCategory() = runTest {
        answerProjectsAndSave()

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.INTERNSHIPS)
        assertThat(state.message).isEqualTo(EvidenceMessage.SAVED)
        assertThat(state.done).isNull()
    }

    @Test
    fun save_onTheLastCategory_showsTheAllDoneSummary() = runTest {
        viewModel.onEnter(FactEvidenceNavKey())
        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.POSITIONS))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Treasurer, coding club"))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.DETAIL, "Managed the event budget."))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.ORGANIZATION, "Example Institute"))

        viewModel.onAction(EvidencePathAction.Save)

        val done = viewModel.uiState.value.done
        assertThat(done).isEqualTo(EvidenceDone(addedCount = 1, skippedCount = 0))
    }

    @Test
    fun save_afterSkippingCountsBothTheFactsAndTheSkipsInTheSummary() = runTest {
        answerProjectsAndSave()
        viewModel.onAction(EvidencePathAction.SkipCategory)
        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.COMPETITIONS))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Smart India Hackathon"))
        viewModel.onAction(EvidencePathAction.NextPrompt)
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.DETAIL, "Finalist, team of 6."))

        viewModel.onAction(EvidencePathAction.Save)
        assertThat(viewModel.uiState.value.done).isNull()
        assertThat(viewModel.uiState.value.category).isEqualTo(EvidenceCategory.POSITIONS)

        viewModel.onAction(EvidencePathAction.SkipCategory)

        assertThat(viewModel.uiState.value.done)
            .isEqualTo(EvidenceDone(addedCount = 2, skippedCount = 2))
    }

    @Test
    fun save_whenTheDetailIsTooLong_showsTheProblemOnThatQuestion() {
        chooseProjects()
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Placement Stats Dashboard"))
        viewModel.onAction(EvidencePathAction.NextPrompt)
        viewModel.onAction(
            EvidencePathAction.AnswerChanged(
                EvidencePrompt.DETAIL,
                "x".repeat(FactDraftValidator.DETAIL_LIMIT + 1),
            ),
        )

        viewModel.onAction(EvidencePathAction.Save)

        val question = viewModel.uiState.value.question
        assertThat(question?.problems).containsEntry(EvidencePrompt.DETAIL, EvidenceFieldProblem.TOO_LONG)
        assertThat(viewModel.uiState.value.cards).isEmpty()
    }

    @Test
    fun save_whenOffline_reassuresThatNothingLeftTheDevice() = runTest {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.OFFLINE))
        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.PROJECTS))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Placement Stats Dashboard"))
        viewModel.onAction(EvidencePathAction.NextPrompt)
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.DETAIL, "Power BI and Excel."))

        viewModel.onAction(EvidencePathAction.Save)

        assertThat(viewModel.uiState.value.message).isEqualTo(EvidenceMessage.OFFLINE_QUEUED)
    }

    @Test
    fun addMore_keepsTheFoldedFactsAndOpensThePicker() = runTest {
        answerProjectsAndSave()
        viewModel.onAction(EvidencePathAction.AddMore)

        val state = viewModel.uiState.value
        assertThat(state.isPicker).isTrue()
        assertThat(state.cards).hasSize(1)
    }

    @Test
    fun finish_clearsTheDoneSummary() = runTest {
        answerProjectsAndSave()
        viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.POSITIONS))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Treasurer"))
        viewModel.onAction(EvidencePathAction.NextPrompt)
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.DETAIL, "Event budget."))
        viewModel.onAction(EvidencePathAction.AnswerChanged(EvidencePrompt.ORGANIZATION, "Example Institute"))

        viewModel.onAction(EvidencePathAction.Save)
        viewModel.onAction(EvidencePathAction.Finish)

        assertThat(viewModel.uiState.value.done).isNull()
    }

    @Test
    fun dismissMessage_clearsTheMessage() {
        chooseProjects()
        viewModel.onAction(EvidencePathAction.NextPrompt)
        viewModel.onAction(EvidencePathAction.Save)

        viewModel.onAction(EvidencePathAction.DismissMessage)

        assertThat(viewModel.uiState.value.message).isNull()
        assertThat(viewModel.uiState.value.isSaveRejected).isFalse()
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() {
        viewModel.onEnter(FactEvidenceNavKey(category = "projects"))
        viewModel.onEnter(FactEvidenceNavKey(category = "competitions"))

        assertThat(viewModel.uiState.value.startCategory).isEqualTo(EvidenceCategory.PROJECTS)
    }

    @Test
    fun scenario_loading_showsTheLoadingState() {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun scenario_offline_showsTheOfflineBanner() {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.OFFLINE))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun scenario_error_reportsThatThePathCouldNotOpen() {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.message).isEqualTo(EvidenceMessage.LOAD_FAILED)
    }

    @Test
    fun scenario_empty_startsOnThePicker() {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.isPicker).isTrue()
    }

    @Test
    fun scenario_userStated_startsOnTheFirstQuestion() {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.USER_STATED))

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(state.question?.prompt).isEqualTo(EvidencePrompt.TITLE)
    }

    @Test
    fun scenario_mapping_coversEveryDebugScenario() {
        DebugScenario.entries.forEach { scenario ->
            val state = evidencePathStateFor(scenario = scenario, category = "projects")
            val isOnlyLoading = scenario == DebugScenario.LOADING || scenario == DebugScenario.DELETING
            assertThat(state.isLoading).isEqualTo(isOnlyLoading)
        }
    }

    @Test
    fun categoryMapping_filesEachCategoryIntoItsProfileSection() {
        assertThat(EvidenceCategory.PROJECTS.entryCategory).isEqualTo(EntryCategory.PROJECT)
        assertThat(EvidenceCategory.INTERNSHIPS.entryCategory).isEqualTo(EntryCategory.EXPERIENCE)
        assertThat(EvidenceCategory.COURSEWORK.entryCategory).isEqualTo(EntryCategory.EDUCATION)
        assertThat(EvidenceCategory.COMPETITIONS.entryCategory).isEqualTo(EntryCategory.ACHIEVEMENT)
        assertThat(EvidenceCategory.POSITIONS.entryCategory).isEqualTo(EntryCategory.ACHIEVEMENT)
    }

    private fun chooseProjects() {
        viewModel.onEnter(FactEvidenceNavKey())
        if (viewModel.uiState.value.category != EvidenceCategory.PROJECTS) {
            viewModel.onAction(EvidencePathAction.CategoryChosen(EvidenceCategory.PROJECTS))
        }
    }

    private suspend fun answerProjectsAndSave() {
        chooseProjects()
        viewModel.onAction(
            EvidencePathAction.AnswerChanged(EvidencePrompt.TITLE, "Placement Stats Dashboard"),
        )
        viewModel.onAction(EvidencePathAction.NextPrompt)
        viewModel.onAction(
            EvidencePathAction.AnswerChanged(
                EvidencePrompt.DETAIL,
                "Power BI and Excel. The T and P cell used it for the 2024 placement report.",
            ),
        )
        viewModel.onAction(EvidencePathAction.Save)
    }

    private fun blankProfile(): CandidateProfile = sampleProfile.copy(entries = emptyList())
}
