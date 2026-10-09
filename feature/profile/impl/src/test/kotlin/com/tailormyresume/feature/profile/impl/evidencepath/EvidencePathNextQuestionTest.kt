package com.tailormyresume.feature.profile.impl.evidencepath

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EvidencePathNextQuestionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val viewModel = EvidencePathViewModel(
        addUserStatedFacts = AddUserStatedFactsUseCase(repository, TestIdGenerator()),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = TestConnectivityMonitor(),
        sessionRepository = session,
    )

    private fun enter(category: String) = viewModel.onEnter(FactEvidenceNavKey(category = category))

    private fun act(action: EvidencePathAction) = viewModel.onAction(action)

    private fun saveAnswer(text: String) {
        act(EvidencePathAction.AnswerChanged(text))
        act(EvidencePathAction.Save)
    }

    @Test
    fun onEnter_questionTotalIsFourForEveryCategory() {
        EVIDENCE_CATEGORIES.forEach { category ->
            assertThat(category.questionCount).isEqualTo(4)
        }
        enter("projects")
        assertThat(viewModel.uiState.value.questionTotal).isEqualTo(4)
    }

    @Test
    fun save_filesAUserStatedFactAndWaitsForNextQuestion() = runTest {
        enter("projects")

        saveAnswer("Library database project. Built with MySQL.")

        val state = viewModel.uiState.value
        assertThat(state.questionNumber).isEqualTo(1)
        assertThat(state.answer).isEmpty()
        val stamped = checkNotNull(state.stamped)
        assertThat(stamped.entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(state.categoryCards.single()).isEqualTo(stamped)
    }

    @Test
    fun nextQuestion_afterASaveMovesOnAndClearsTheStamp() = runTest {
        enter("projects")
        saveAnswer("Library database project")
        assertThat(viewModel.uiState.value.stamped).isNotNull()

        act(EvidencePathAction.NextQuestion)

        val state = viewModel.uiState.value
        assertThat(state.questionNumber).isEqualTo(2)
        assertThat(state.stamped).isNull()
        assertThat(state.category).isEqualTo(EvidenceCategory.PROJECTS)
    }

    @Test
    fun projectName_isTheTitleOfTheFirstFactFiledInTheCategory() = runTest {
        enter("projects")
        assertThat(viewModel.uiState.value.projectName).isNull()

        saveAnswer("Library database project. Built with MySQL.")

        assertThat(viewModel.uiState.value.projectName).isEqualTo("Library database project")
    }

    @Test
    fun skip_movesToTheNextQuestionOfTheSameCategoryAndNotesTheSkip() {
        enter("projects")

        act(EvidencePathAction.Skip)

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(state.questionNumber).isEqualTo(2)
        assertThat(state.skipNote).isEqualTo(EvidenceSkipNote(EvidenceCategory.PROJECTS, 1))
        act(EvidencePathAction.Skip)
        assertThat(viewModel.uiState.value.category).isEqualTo(EvidenceCategory.PROJECTS)
        assertThat(viewModel.uiState.value.questionNumber).isEqualTo(3)
    }

    @Test
    fun skip_onTheLastQuestionEndsTheCategoryWithoutOpeningAnother() {
        enter("projects")

        repeat(4) { act(EvidencePathAction.Skip) }

        val state = viewModel.uiState.value
        assertThat(state.isDone).isTrue()
        assertThat(state.category).isNull()
    }

    @Test
    fun skippingTheLastQuestionOfACategory_endsOnAllDone() {
        enter("")
        act(EvidencePathAction.CategoryChosen(EvidenceCategory.INTERNSHIPS))

        repeat(4) { act(EvidencePathAction.Skip) }

        assertThat(viewModel.uiState.value.isDone).isTrue()
    }

    @Test
    fun nextQuestion_afterSavingTheLastQuestionEndsOnAllDoneWithEveryFact() = runTest {
        enter("work")
        repeat(3) { act(EvidencePathAction.Skip) }
        saveAnswer("Weekly sales reports, 40 stores")
        assertThat(viewModel.uiState.value.isDone).isFalse()

        act(EvidencePathAction.NextQuestion)

        val state = viewModel.uiState.value
        assertThat(state.isDone).isTrue()
        assertThat(state.category).isNull()
        assertThat(state.cards).hasSize(1)
    }

    @Test
    fun addMore_afterASaveReturnsToThePickerAndKeepsTheCards() = runTest {
        enter("work")
        saveAnswer("Weekly sales reports, 40 stores")
        assertThat(viewModel.uiState.value.category).isEqualTo(EvidenceCategory.WORK)
        assertThat(viewModel.uiState.value.stamped).isNotNull()

        act(EvidencePathAction.AddMore)

        val state = viewModel.uiState.value
        assertThat(state.isPicker).isTrue()
        assertThat(state.stamped).isNull()
        assertThat(state.cards).hasSize(1)
    }
}
