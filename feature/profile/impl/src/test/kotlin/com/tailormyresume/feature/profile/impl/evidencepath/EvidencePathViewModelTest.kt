package com.tailormyresume.feature.profile.impl.evidencepath

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.fact.FactDraftValidator
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.impl.ProfileExit
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EvidencePathViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val connectivity = TestConnectivityMonitor()
    private val viewModel = EvidencePathViewModel(
        addUserStatedFacts = AddUserStatedFactsUseCase(repository, TestIdGenerator()),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = connectivity,
        sessionRepository = session,
    )

    private val dashboardAnswer =
        "Placement Stats Dashboard. Power BI and Excel. The T&P cell used it for the 2024 placement report."

    private fun enter(category: String = "") = viewModel.onEnter(FactEvidenceNavKey(category = category))

    private fun act(action: EvidencePathAction) = viewModel.onAction(action)

    private fun answer(text: String) = act(EvidencePathAction.AnswerChanged(text))

    @Test
    fun onEnter_forSomeoneJustStartingOut_asksAboutProjectsAndInternshipsFirst() = runTest {
        session.saveCareerStage(CareerStage.JUST_STARTING_OUT)

        enter()

        val order = viewModel.uiState.first().categoryOrder
        assertThat(order.take(2)).containsExactly(EvidenceCategory.PROJECTS, EvidenceCategory.INTERNSHIPS).inOrder()
        assertThat(order.last()).isEqualTo(EvidenceCategory.WORK)
    }

    @Test
    fun onEnter_forSomeoneOneToTwoYearsIn_asksAboutWorkFirst() = runTest {
        session.saveCareerStage(CareerStage.ONE_TO_TWO_YEARS_IN)

        enter()

        assertThat(viewModel.uiState.first().categoryOrder.first()).isEqualTo(EvidenceCategory.WORK)
    }

    @Test
    fun skippingTheLastQuestionOfACategory_movesToTheNextOneInTheStoredOrder() = runTest {
        session.saveCareerStage(CareerStage.JUST_STARTING_OUT)
        enter()

        act(EvidencePathAction.CategoryChosen(EvidenceCategory.INTERNSHIPS))
        act(EvidencePathAction.Skip)

        assertThat(viewModel.uiState.value.category).isEqualTo(EvidenceCategory.PROJECTS)
    }

    @Test
    fun splitAnswer_usesTheFirstSentenceAsTheTitleAndKeepsTheRestAsDetail() {
        val parts = splitAnswer(dashboardAnswer)

        assertThat(parts.title).isEqualTo("Placement Stats Dashboard")
        assertThat(parts.detail).isEqualTo("Power BI and Excel. The T&P cell used it for the 2024 placement report.")
    }

    @Test
    fun splitAnswer_splitsOnTheFirstLineBreak() {
        val parts = splitAnswer("Treasurer, coding club\nManaged a 40,000 rupee event budget")

        assertThat(parts.title).isEqualTo("Treasurer, coding club")
        assertThat(parts.detail).isEqualTo("Managed a 40,000 rupee event budget")
    }

    @Test
    fun splitAnswer_aShortAnswerIsAllTitle() {
        val parts = splitAnswer("  Weekly sales reports, 40 stores.  ")

        assertThat(parts.title).isEqualTo("Weekly sales reports, 40 stores")
        assertThat(parts.detail).isEmpty()
    }

    @Test
    fun splitAnswer_aLongFirstSentenceMovesTheOverflowIntoTheDetail() {
        val long = "word ".repeat(30).trim()

        val parts = splitAnswer(long)

        assertThat(parts.title.length).isAtMost(80)
        assertThat("${parts.title} ${parts.detail}").isEqualTo(long)
    }

    @Test
    fun onEnter_withoutACategoryShowsThePicker() {
        enter()

        assertThat(viewModel.uiState.value.isPicker).isTrue()
    }

    @Test
    fun onEnter_withACategoryStartsOnItsFirstQuestion() {
        enter("projects")

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(state.questionNumber).isEqualTo(1)
        assertThat(state.questionTotal).isEqualTo(2)
    }

    @Test
    fun onEnter_forcedScenariosSeedTheState() {
        viewModel.onEnter(FactEvidenceNavKey(scenario = DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.message).isEqualTo(EvidenceMessage.LOAD_FAILED)
    }

    @Test
    fun connectivity_marksThePathOffline() {
        enter()

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun categories_includeWorkWithOneQuestion() {
        assertThat(EVIDENCE_CATEGORIES.map { it.key }).containsExactly(
            "work",
            "projects",
            "internships",
            "coursework",
            "competitions",
            "positions",
        ).inOrder()
        assertThat(EvidenceCategory.WORK.questionCount).isEqualTo(1)
        assertThat(EvidenceCategory.WORK.entryCategory).isEqualTo(EntryCategory.EXPERIENCE)
    }

    @Test
    fun save_filesAUserStatedFactAndMovesToTheNextQuestion() = runTest {
        enter("projects")
        answer(dashboardAnswer)

        act(EvidencePathAction.Save)

        val state = viewModel.uiState.value
        assertThat(state.questionNumber).isEqualTo(2)
        assertThat(state.answer).isEmpty()
        val card = state.categoryCards.single()
        assertThat(card.entry.title).isEqualTo("Placement Stats Dashboard")
        assertThat(card.entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(repository.observeProfile().first().let(::checkNotNull).entries.single().id).isEqualTo(card.entry.id)
    }

    @Test
    fun save_withABlankAnswerDoesNothing() = runTest {
        enter("projects")
        answer("   ")

        act(EvidencePathAction.Save)

        assertThat(viewModel.uiState.value.cards).isEmpty()
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun save_withAnAnswerThatIsTooLongFlagsTheAnswer() = runTest {
        enter("projects")
        answer("Title. " + "x".repeat(FactDraftValidator.DETAIL_LIMIT + 1))

        act(EvidencePathAction.Save)

        assertThat(viewModel.uiState.value.problem).isEqualTo(EvidenceFieldProblem.TOO_LONG)
        assertThat(viewModel.uiState.value.cards).isEmpty()
    }

    @Test
    fun skip_onTheLastQuestionMovesToTheFirstUnvisitedCategoryAndNotesTheSkip() {
        enter("projects")
        act(EvidencePathAction.Skip)
        assertThat(viewModel.uiState.value.questionNumber).isEqualTo(2)

        act(EvidencePathAction.Skip)

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.WORK)
        assertThat(state.skipNote).isEqualTo(EvidenceSkipNote(EvidenceCategory.PROJECTS, 2))
    }

    @Test
    fun skippingEveryQuestion_endsOnTheAllDoneState() {
        enter("projects")
        repeat(EVIDENCE_CATEGORIES.sumOf { it.questionCount }) { act(EvidencePathAction.Skip) }

        val state = viewModel.uiState.value
        assertThat(state.isDone).isTrue()
        assertThat(state.cards).isEmpty()
        assertThat(state.category).isNull()
    }

    @Test
    fun chooseCategory_jumpsThereAndClearsTheDraft() {
        enter()
        act(EvidencePathAction.CategoryChosen(EvidenceCategory.COURSEWORK))
        answer("DBMS")

        act(EvidencePathAction.CategoryChosen(EvidenceCategory.WORK))

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.WORK)
        assertThat(state.answer).isEmpty()
        assertThat(state.questionNumber).isEqualTo(1)
    }

    @Test
    fun addMore_returnsToThePickerAndKeepsTheSavedCards() = runTest {
        enter("work")
        answer("Weekly sales reports, 40 stores")
        act(EvidencePathAction.Save)
        assertThat(viewModel.uiState.value.category).isEqualTo(EvidenceCategory.PROJECTS)

        act(EvidencePathAction.AddMore)

        val state = viewModel.uiState.value
        assertThat(state.isPicker).isTrue()
        assertThat(state.cards).hasSize(1)
    }

    @Test
    fun finish_whenOnboardingIsNotComplete_followsTheNextOnboardingStep() = runTest {
        enter()

        act(EvidencePathAction.Finish)

        assertThat(viewModel.uiState.value.navigation)
            .isEqualTo(EvidenceNavigation.Exit(ProfileExit.Step(SignInNavKey())))
        act(EvidencePathAction.NavigationConsumed)
        assertThat(viewModel.uiState.value.navigation).isNull()
    }

    @Test
    fun finish_whenOnboardingIsComplete_goesBackToTheProfile() = runTest {
        session.sendOnboardingComplete(true)
        enter()

        act(EvidencePathAction.Finish)

        assertThat(viewModel.uiState.value.navigation).isEqualTo(EvidenceNavigation.Exit(ProfileExit.Profile))
    }
}
