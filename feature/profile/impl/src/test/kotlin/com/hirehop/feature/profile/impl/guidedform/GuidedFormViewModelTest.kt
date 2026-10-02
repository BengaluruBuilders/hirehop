package com.hirehop.feature.profile.impl.guidedform

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestIdGenerator
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.hirehop.feature.profile.impl.ProfileExit
import com.hirehop.feature.profile.impl.ProfileExitResolver
import com.hirehop.feature.profile.impl.UserFactWriter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GuidedFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val connectivity = TestConnectivityMonitor()
    private val viewModel = GuidedFormViewModel(
        factWriter = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator())),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = connectivity,
    )

    private fun enter(key: GuidedProfileFormNavKey = GuidedProfileFormNavKey()) = viewModel.onEnter(key)

    private fun type(field: GuidedField, value: String) =
        viewModel.onAction(GuidedFormAction.ValueChanged(field, value))

    private fun act(action: GuidedFormAction) = viewModel.onAction(action)

    private fun fillEducation() {
        type(GuidedField.COURSE, "B.Tech Computer Science")
        type(GuidedField.COLLEGE, "Savitribai Phule Pune University")
        type(GuidedField.EDUCATION_END, "2024")
        type(GuidedField.COURSEWORK, "DBMS, Probability & Statistics")
    }

    @Test
    fun onEnter_startsOnContactAndIgnoresASecondEnter() {
        enter(GuidedProfileFormNavKey(startStep = "skills"))
        enter(GuidedProfileFormNavKey(startStep = "education"))

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.SKILLS)
    }

    @Test
    fun onEnter_scannedArrivalShowsTheIntroUntilTheFormStarts() {
        enter(GuidedProfileFormNavKey(resumedFromScan = true))

        assertThat(viewModel.uiState.value.arrival).isEqualTo(GuidedArrival.FROM_SCANNED_PDF)
        assertThat(viewModel.uiState.value.showIntro).isTrue()

        act(GuidedFormAction.StartForm)

        assertThat(viewModel.uiState.value.showIntro).isFalse()
    }

    @Test
    fun onEnter_forcedScenariosSeedTheState() {
        enter(GuidedProfileFormNavKey(scenario = DebugScenario.LOADING))
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun connectivity_marksTheFormOffline() {
        enter()
        assertThat(viewModel.uiState.value.isOffline).isFalse()

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun next_onContact_createsTheProfileAndAdvances() = runTest {
        enter()
        type(GuidedField.FULL_NAME, "Priya Deshmukh")
        type(GuidedField.EMAIL, "priya.d@example.com")

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(viewModel.uiState.value.completedSteps).containsExactly(GuidedStep.CONTACT)
        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.fullName).isEqualTo("Priya Deshmukh")
        assertThat(saved.email).isEqualTo("priya.d@example.com")
    }

    @Test
    fun next_onEmptyContact_advancesWithoutCreatingAProfile() = runTest {
        enter()

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(viewModel.uiState.value.completedSteps).isEmpty()
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun next_onEducation_filesTwoFactsAndShowsThemOnTheNextStep() = runTest {
        enter(GuidedProfileFormNavKey(startStep = "education"))
        fillEducation()

        act(GuidedFormAction.Next)

        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(GuidedStep.SKILLS)
        assertThat(state.filedEntries).hasSize(2)
        assertThat(state.filedEntries.map { it.category }.toSet()).containsExactly(EntryCategory.EDUCATION)
        val degree = state.filedEntries.first()
        assertThat(degree.title).isEqualTo("B.Tech Computer Science")
        assertThat(degree.organization).isEqualTo("Savitribai Phule Pune University")
        assertThat(degree.endDate).isEqualTo("2024")
        assertThat(degree.source).isEqualTo(FactSource.USER_STATED)
        assertThat(repository.observeProfile().first().let(::checkNotNull).entries).hasSize(2)
    }

    @Test
    fun next_onEducationWithAnInstituteButNoDegree_flagsTheDegreeAndStays() = runTest {
        enter(GuidedProfileFormNavKey(startStep = "education"))
        type(GuidedField.COLLEGE, "Pune University")

        act(GuidedFormAction.Next)

        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(state.fieldProblems[GuidedField.COURSE]).isEqualTo(GuidedFieldProblem.REQUIRED)
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun next_onEducationAgainAfterGoingBack_replacesTheEarlierFacts() = runTest {
        enter(GuidedProfileFormNavKey(startStep = "education"))
        fillEducation()
        act(GuidedFormAction.Next)
        act(GuidedFormAction.Back)
        type(GuidedField.COURSE, "B.Sc Statistics")

        act(GuidedFormAction.Next)

        val entries = repository.observeProfile().first().let(::checkNotNull).entries
        assertThat(entries).hasSize(2)
        assertThat(entries.map { it.title }).contains("B.Sc Statistics")
        assertThat(entries.map { it.title }).doesNotContain("B.Tech Computer Science")
    }

    @Test
    fun skills_addCommitsTheTypedSkillAndNextSavesThemAll() = runTest {
        enter(GuidedProfileFormNavKey(startStep = "skills"))
        type(GuidedField.SKILL, "SQL")
        act(GuidedFormAction.AddSkill)
        type(GuidedField.SKILL, "Excel")
        act(GuidedFormAction.AddSkill)
        type(GuidedField.SKILL, "sql")
        act(GuidedFormAction.AddSkill)
        type(GuidedField.SKILL, "Power BI")

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.skills).containsExactly("SQL", "Excel", "Power BI").inOrder()
        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EXPERIENCE)
        assertThat(repository.observeProfile().first().let(::checkNotNull).skills).containsExactly("SQL", "Excel", "Power BI").inOrder()
    }

    @Test
    fun skills_removeDropsTheChip() {
        enter(GuidedProfileFormNavKey(startStep = "skills"))
        type(GuidedField.SKILL, "SQL")
        act(GuidedFormAction.AddSkill)

        act(GuidedFormAction.RemoveSkill("SQL"))

        assertThat(viewModel.uiState.value.skills).isEmpty()
    }

    @Test
    fun back_returnsToThePreviousStepAndClearsTheFiledCards() = runTest {
        enter(GuidedProfileFormNavKey(startStep = "education"))
        fillEducation()
        act(GuidedFormAction.Next)

        act(GuidedFormAction.Back)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(viewModel.uiState.value.filedEntries).isEmpty()
    }

    @Test
    fun saveAndFinishLater_savesTheCurrentStepAndListsTheCreatedFacts() = runTest {
        enter(GuidedProfileFormNavKey(startStep = "education"))
        fillEducation()

        act(GuidedFormAction.SaveAndFinishLater)

        val saved = viewModel.uiState.value.saved.let(::checkNotNull)
        assertThat(saved.completedSteps).isEqualTo(1)
        assertThat(saved.totalSteps).isEqualTo(4)
        assertThat(saved.entryIds).hasSize(2)
        assertThat(repository.observeProfile().first().let(::checkNotNull).entries).hasSize(2)
    }

    @Test
    fun saveAndFinishLater_onTheScannedIntroSavesNothing() = runTest {
        enter(GuidedProfileFormNavKey(resumedFromScan = true))

        act(GuidedFormAction.SaveAndFinishLater)

        assertThat(viewModel.uiState.value.saved.let(::checkNotNull).completedSteps).isEqualTo(0)
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun next_onTheLastStep_handsOffToTheProjectsEvidencePath() {
        enter(GuidedProfileFormNavKey(startStep = "experience"))

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.navigation).isEqualTo(GuidedNavigation.Evidence("projects"))
        act(GuidedFormAction.NavigationConsumed)
        assertThat(viewModel.uiState.value.navigation).isNull()
    }

    @Test
    fun goToProjects_handsOffToTheProjectsEvidencePath() {
        enter(GuidedProfileFormNavKey(startStep = "experience"))

        act(GuidedFormAction.GoToProjects)

        assertThat(viewModel.uiState.value.navigation).isEqualTo(GuidedNavigation.Evidence("projects"))
    }

    @Test
    fun finishSaved_whenOnboardingIsNotComplete_followsTheNextOnboardingStep() = runTest {
        enter()

        act(GuidedFormAction.FinishSaved)

        assertThat(viewModel.uiState.value.navigation)
            .isEqualTo(GuidedNavigation.Exit(ProfileExit.Step(SignInNavKey())))
    }

    @Test
    fun finishSaved_whenOnboardingIsComplete_goesBackToTheProfile() = runTest {
        session.sendOnboardingComplete(true)
        enter()

        act(GuidedFormAction.FinishSaved)

        assertThat(viewModel.uiState.value.navigation).isEqualTo(GuidedNavigation.Exit(ProfileExit.Profile))
    }
}
