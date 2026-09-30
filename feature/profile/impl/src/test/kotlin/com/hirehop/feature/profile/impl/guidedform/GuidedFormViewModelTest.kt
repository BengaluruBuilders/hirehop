package com.hirehop.feature.profile.impl.guidedform

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GuidedFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private var nextBulletId = 0

    private val idGenerator = IdGenerator { "bullet-${nextBulletId++}" }

    private lateinit var viewModel: GuidedFormViewModel

    @Before
    fun setup() {
        repository.sendProfile(blankProfile())
        viewModel = GuidedFormViewModel(
            addUserStatedFacts = AddUserStatedFactsUseCase(
                profileRepository = repository,
                idGenerator = idGenerator,
            ),
        )
    }

    @Test
    fun onEnter_startsOnContactStep() {
        viewModel.onEnter(GuidedProfileFormNavKey())

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.CONTACT)
        assertThat(viewModel.uiState.value.stepIndex).isEqualTo(0)
        assertThat(viewModel.uiState.value.isFirstStep).isTrue()
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() {
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = "skills"))
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = "education"))

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.SKILLS)
    }

    @Test
    fun onNext_advancesOneStepAtATime() {
        enter()

        viewModel.onAction(GuidedFormAction.Next)
        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)

        answerEducation()
        viewModel.onAction(GuidedFormAction.Next)
        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.SKILLS)

        viewModel.onAction(GuidedFormAction.Next)
        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EXPERIENCE)
        assertThat(viewModel.uiState.value.isLastStep).isTrue()
    }

    @Test
    fun onNext_onLastStep_doesNotRunPastTheEnd() {
        enter("skills")

        repeat(6) { viewModel.onAction(GuidedFormAction.Next) }

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EXPERIENCE)
    }

    @Test
    fun onBack_returnsToThePreviousStep() {
        enter()
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.Next)
        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.Back)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)
    }

    @Test
    fun onBack_onFirstStep_staysPut() {
        enter()

        viewModel.onAction(GuidedFormAction.Back)

        assertThat(viewModel.uiState.value.stepIndex).isEqualTo(0)
    }

    @Test
    fun valueChanged_keepsWhatTheUserTyped() {
        enter()

        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.FULL_NAME, "Priya Deshmukh"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EMAIL, "priya@example.com"))

        val values = viewModel.uiState.value.values
        assertThat(values[GuidedField.FULL_NAME]).isEqualTo("Priya Deshmukh")
        assertThat(values[GuidedField.EMAIL]).isEqualTo("priya@example.com")
    }

    @Test
    fun skillsStep_splitsTypedLinesIntoSkills() {
        enter("skills")

        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.SKILL, "Kotlin\nSQL\nkotlin\n"))

        assertThat(viewModel.uiState.value.skills).containsExactly("Kotlin", "SQL").inOrder()
    }

    @Test
    fun next_whenEducationTitleIsBlank_showsTheFieldProblemAndStays() {
        enter("education")

        viewModel.onAction(GuidedFormAction.Next)

        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(state.fieldProblems[GuidedField.COURSE]).isEqualTo(GuidedFieldProblem.REQUIRED)
        assertThat(state.isSaveRejected).isTrue()
    }

    @Test
    fun next_whenEndDateIsBeforeStartDate_showsTheFieldProblem() {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_START, "2024"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_END, "2022"))

        viewModel.onAction(GuidedFormAction.Next)

        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(state.fieldProblems[GuidedField.EDUCATION_END])
            .isEqualTo(GuidedFieldProblem.END_BEFORE_START)
    }

    @Test
    fun next_whenEducationIsValid_foldsTheFactPreview() {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Example Institute"))

        viewModel.onAction(GuidedFormAction.Next)

        val preview = viewModel.uiState.value.previews.single()
        assertThat(preview.category).isEqualTo(EntryCategory.EDUCATION)
        assertThat(preview.line).contains("B.Tech Computer Science")
        assertThat(preview.entry).isNull()
    }

    @Test
    fun startHandoff_beforeTheLastStep_doesNothing() {
        enter("education")

        viewModel.onAction(GuidedFormAction.StartHandoff)

        assertThat(viewModel.uiState.value.handoff).isNull()
    }

    @Test
    fun startHandoff_onTheLastStep_offersTheEvidencePath() {
        enter("experience")

        viewModel.onAction(GuidedFormAction.StartHandoff)

        assertThat(viewModel.uiState.value.handoff)
            .isEqualTo(GuidedHandoff(category = "projects"))
    }

    @Test
    fun handoffConsumed_clearsTheHandoff() {
        enter("experience")
        viewModel.onAction(GuidedFormAction.StartHandoff)

        viewModel.onAction(GuidedFormAction.HandoffConsumed)

        assertThat(viewModel.uiState.value.handoff).isNull()
    }

    @Test
    fun saveAndFinishLater_reportsSavedAndCountsTheSteps() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val state = viewModel.uiState.value
        assertThat(state.isSaving).isFalse()
        assertThat(state.saved).isEqualTo(GuidedSaved(completedSteps = 1, totalSteps = 4))
        assertThat(state.message).isEqualTo(GuidedMessage.SAVED)
    }

    @Test
    fun saveAndFinishLater_factsLandInTheProfileAsUserStated() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Example Institute"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_START, "2022"))
        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val saved = checkNotNull(repository.observeProfile().first()) { "Expected a saved profile" }
        val added = saved.entries.single { it.title == "B.Tech Computer Science" }
        assertThat(added.category).isEqualTo(EntryCategory.EDUCATION)
        assertThat(added.organization).isEqualTo("Example Institute")
        assertThat(added.source).isEqualTo(FactSource.USER_STATED)
        assertThat(added.isConfirmed).isFalse()
    }

    @Test
    fun saveAndFinishLater_foldsTheRealFactIdIntoThePreview() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val entry = viewModel.uiState.value.previews.single().entry
        assertThat(entry).isNotNull()
        assertThat(entry?.source).isEqualTo(FactSource.USER_STATED)
    }

    @Test
    fun saveAndFinishLater_withNoFactSteps_savesNothingAndSaysSo() = runTest {
        enter()

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val state = viewModel.uiState.value
        assertThat(state.saved).isEqualTo(GuidedSaved(completedSteps = 0, totalSteps = 4))
        assertThat(state.previews).isEmpty()
    }

    @Test
    fun saveAndFinishLater_whenTheUserClearsACompletedTitle_isRejected() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Example Institute"))
        viewModel.onAction(GuidedFormAction.Next)
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "   "))

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val state = viewModel.uiState.value
        assertThat(state.isSaveRejected).isTrue()
        assertThat(state.message).isEqualTo(GuidedMessage.SAVE_REJECTED)
        assertThat(state.fieldProblems[GuidedField.COURSE]).isEqualTo(GuidedFieldProblem.REQUIRED)
        val saved = checkNotNull(repository.observeProfile().first())
        assertThat(saved.entries).isEmpty()
    }

    @Test
    fun saveAndFinishLater_whenOneStepHasTwoProblems_showsEveryErrorAndWritesNothing() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Example Institute"))
        viewModel.onAction(GuidedFormAction.Next)
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "   "))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_START, "2024"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_END, "2022"))

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val problems = viewModel.uiState.value.fieldProblems
        assertThat(problems[GuidedField.COURSE]).isEqualTo(GuidedFieldProblem.REQUIRED)
        assertThat(problems[GuidedField.EDUCATION_END]).isEqualTo(GuidedFieldProblem.END_BEFORE_START)
        val saved = checkNotNull(repository.observeProfile().first())
        assertThat(saved.entries).isEmpty()
    }

    @Test
    fun saveAndFinishLater_whenACompletedStepIsEntirelyBlank_addsNothing() = runTest {
        enter("skills")

        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        val saved = checkNotNull(repository.observeProfile().first())
        assertThat(saved.entries).isEmpty()
        assertThat(viewModel.uiState.value.isSaveRejected).isFalse()
    }

    @Test
    fun saveAndFinishLater_whenOffline_reassuresThatNothingLeftTheDevice() = runTest {
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = "education", scenario = DebugScenario.OFFLINE))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        assertThat(viewModel.uiState.value.message).isEqualTo(GuidedMessage.OFFLINE_QUEUED)
    }

    @Test
    fun continueNow_dismissesTheSavedCard() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.Next)
        viewModel.onAction(GuidedFormAction.SaveAndFinishLater)

        viewModel.onAction(GuidedFormAction.ContinueNow)

        assertThat(viewModel.uiState.value.saved).isNull()
    }

    @Test
    fun dismissMessage_clearsTheMessage() = runTest {
        enter("education")
        viewModel.onAction(GuidedFormAction.Next)

        viewModel.onAction(GuidedFormAction.DismissMessage)

        assertThat(viewModel.uiState.value.message).isNull()
        assertThat(viewModel.uiState.value.isSaveRejected).isFalse()
    }

    @Test
    fun scenario_scanned_showsTheScannedArrival() {
        viewModel.onEnter(GuidedProfileFormNavKey(scenario = DebugScenario.SCANNED))

        assertThat(viewModel.uiState.value.arrival).isEqualTo(GuidedArrival.FROM_SCANNED_PDF)
    }

    @Test
    fun scenario_resumedFromScan_showsTheScannedArrival() {
        viewModel.onEnter(GuidedProfileFormNavKey(resumedFromScan = true))

        assertThat(viewModel.uiState.value.arrival).isEqualTo(GuidedArrival.FROM_SCANNED_PDF)
    }

    @Test
    fun scenario_offline_showsTheOfflineBanner() {
        viewModel.onEnter(GuidedProfileFormNavKey(scenario = DebugScenario.OFFLINE))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun scenario_loading_showsTheLoadingState() {
        viewModel.onEnter(GuidedProfileFormNavKey(scenario = DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun scenario_error_reportsThatTheFormCouldNotOpen() {
        viewModel.onEnter(GuidedProfileFormNavKey(scenario = DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.message).isEqualTo(GuidedMessage.LOAD_FAILED)
    }

    @Test
    fun scenario_mapping_coversEveryDebugScenarioWithoutLoading() {
        DebugScenario.entries.forEach { scenario ->
            val state = guidedFormStateFor(
                scenario = scenario,
                startStep = "contact",
                resumedFromScan = false,
            )
            val isOnlyLoading = scenario == DebugScenario.LOADING || scenario == DebugScenario.DELETING
            assertThat(state.isLoading).isEqualTo(isOnlyLoading)
        }
    }

    @Test
    fun scenario_mapping_startsAtTheNamedStep() {
        val state = guidedFormStateFor(
            scenario = DebugScenario.DEFAULT,
            startStep = "skills",
            resumedFromScan = false,
        )

        assertThat(state.step).isEqualTo(GuidedStep.SKILLS)
        assertThat(state.stepIndex).isEqualTo(2)
    }

    @Test
    fun scenario_mapping_withAnUnknownStep_fallsBackToContact() {
        val state = guidedFormStateFor(
            scenario = DebugScenario.DEFAULT,
            startStep = "not-a-step",
            resumedFromScan = false,
        )

        assertThat(state.step).isEqualTo(GuidedStep.CONTACT)
    }

    private fun enter(startStep: String = "contact") {
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = startStep))
    }

    private fun answerEducation() {
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
    }

    private fun blankProfile(): CandidateProfile = sampleProfile.copy(entries = emptyList())
}
