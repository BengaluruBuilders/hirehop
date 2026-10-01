package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApplicationDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = RecordingApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val prepQuestionSource = TestPrepQuestionSource { _, _ -> PREP_QUESTION_COUNT }
    private val application = testApplication(
        id = APPLICATION_ID,
        notes = "Saved notes",
        gapAnalysis = testGapAnalysis(),
        tailoredResume = testTailoredResume(),
    )
    private val applicationScope = CoroutineScope(UnconfinedTestDispatcher())
    private val viewModelStore = ViewModelStore()
    private lateinit var viewModel: ApplicationDetailViewModel

    @Before
    fun setUp() {
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    ApplicationDetailViewModel(
                        applicationRepository = applicationRepository,
                        profileRepository = profileRepository,
                        prepQuestionSource = prepQuestionSource,
                        applicationScope = applicationScope,
                        applicationId = APPLICATION_ID,
                    )
                }
            },
        )[ApplicationDetailViewModel::class]
    }

    @After
    fun tearDown() {
        applicationScope.cancel()
    }

    @Test
    fun applicationId_matchesAssistedValue() {
        assertThat(viewModel.applicationId).isEqualTo(APPLICATION_ID)
    }

    @Test
    fun uiState_beforeRepositoryEmits_isLoading() {
        assertThat(viewModel.uiState.value).isEqualTo(ApplicationDetailUiState.Loading)
    }

    @Test
    fun uiState_whenTheApplicationIsGone_isNotFound() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(emptyList())
            runCurrent()

            assertThat(current()).isEqualTo(ApplicationDetailUiState.NotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheApplicationExists_isReadyWithItsStoredFacts() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            profileRepository.sendProfile(canonicalCandidateProfile)
            runCurrent()

            val state = current().ready()
            assertThat(state.jobTitle).isEqualTo("Role $APPLICATION_ID")
            assertThat(state.company).isEqualTo("Company $APPLICATION_ID")
            assertThat(state.status).isEqualTo(ApplicationStatus.SAVED)
            assertThat(state.notes).isEqualTo("Saved notes")
            assertThat(state.coverage?.covered).isEqualTo(3)
            assertThat(state.coverage?.total).isEqualTo(5)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenAGapAnalysisExists_countsMetPartialAndGap() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            assertThat(current().ready().gapCounts)
                .isEqualTo(WorkspaceGapCounts(met = 2, partial = 1, gap = 2))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenAGapAnalysisExists_hasOnePrepTaskPerGapAndNoTaskForMetRequirements() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            val tasks = current().ready().prepTasks
            assertThat(tasks.map { task -> task.id }).containsExactly("d", "e").inOrder()
            assertThat(tasks.none { task -> task.isDone }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheProfileExists_reportsTheRealPrepQuestionCount() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            profileRepository.sendProfile(canonicalCandidateProfile)
            runCurrent()

            assertThat(current().ready().prepQuestionCount).isEqualTo(PREP_QUESTION_COUNT)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenThereIsNoTailoredResume_isAbsent() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(application.copy(tailoredResume = null)),
            )

            assertThat(current().ready().resume).isEqualTo(WorkspaceResume.Absent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenATailoredResumeExists_isNotExported() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            assertThat(current().ready().resume).isEqualTo(WorkspaceResume.NotExported)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_defaultScenario_isNotOffline() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            assertThat(current().ready().isOffline).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_offlineScenario_readsEverywhere() = runTest {
        viewModel.onEnter(DebugScenario.OFFLINE)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            val state = current().ready()
            assertThat(state.isOffline).isTrue()
            assertThat(state.gapCounts).isNotNull()
            assertThat(state.prepTasks).isNotEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_theJobDescriptionStartsCollapsed() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            assertThat(current().ready().isJobDescriptionExpanded).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_jobDescriptionToggled_expandsTheStoredJobDescription() = runTest {
        val withText = application.copy(
            job = application.job.copy(rawText = "Northwind GCC is hiring."),
        )

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(withText))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.JobDescriptionToggled)

            val state = current().ready()
            assertThat(state.isJobDescriptionExpanded).isTrue()
            assertThat(state.jobDescriptionText).isEqualTo("Northwind GCC is hiring.")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskToggled_marksOneTaskDoneAndOnlyThatOne() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskToggled("d"))

            val tasks = current().ready().prepTasks
            assertThat(tasks.single { task -> task.isDone }.id).isEqualTo("d")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskToggled_again_clearsTheTick() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskToggled("d"))

            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskToggled("d"))

            assertThat(current().ready().prepTasks.none { task -> task.isDone }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskOverflowToggled_opensOneOverflowAtATime() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskOverflowToggled("d"))
            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskOverflowToggled("e"))

            val tasks = current().ready().prepTasks
            assertThat(tasks.single { task -> task.isOverflowOpen }.id).isEqualTo("e")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskInaccuracyReported_closesTheOverflowAndConfirms() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskOverflowToggled("d"))

            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskInaccuracyReported("d"))

            val state = current().ready()
            assertThat(state.prepTasks.none { task -> task.isOverflowOpen }).isTrue()
            assertThat(state.message).isEqualTo(WorkspaceMessage(text = WorkspaceMessageText.ReportedInaccurate))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_notesChanged_autosavesAndSaysJustNow() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.NotesChanged("Round 1 is a SQL test."))
            advanceTimeBy(1_000)
            runCurrent()

            assertThat(applicationRepository.notesWrites).containsExactly("Round 1 is a SQL test.")
            assertThat(current().ready().notesState).isEqualTo(WorkspaceNotesState.SavedJustNow)
            assertThat(current().ready().notes).isEqualTo("Round 1 is a SQL test.")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_notesFocusChanged_isHeldForTheField() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.NotesFocusChanged(true))

            assertThat(current().ready().isNotesFocused).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusChipChosen_opensTheStatusSheetWithTheStoredStatus() = runTest {
        val interviewing = application.copy(status = ApplicationStatus.INTERVIEW)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(interviewing))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.StatusChipChosen)

            assertThat(current().ready().statusSheet)
                .isEqualTo(ApplicationStatusSheetState(APPLICATION_ID, ApplicationStatus.INTERVIEW))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusChosen_writesTheNewStatusAndClosesTheSheet() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.StatusChipChosen)

            viewModel.onAction(ApplicationWorkspaceAction.StatusChosen(ApplicationStatus.INTERVIEW))

            val state = current().ready()
            assertThat(state.statusSheet).isNull()
            assertThat(state.status).isEqualTo(ApplicationStatus.INTERVIEW)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_moreChosen_opensTheOverflow() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.MoreChosen)

            assertThat(current().ready().isMoreOpen).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_deleteChosen_opensTheDialogWithTheRealScopeAndTheRealFactCount() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            profileRepository.sendProfile(canonicalCandidateProfile)
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.DeleteChosen)

            val state = current().ready()
            assertThat(state.isDeleteDialogVisible).isTrue()
            assertThat(state.isMoreOpen).isFalse()
            val scope = state.deleteScope
            assertThat(scope?.hasGapAnalysis).isTrue()
            assertThat(scope?.hasTailoredResume).isTrue()
            assertThat(scope?.hasNotes).isTrue()
            assertThat(scope?.prepTaskCount).isEqualTo(2)
            assertThat(scope?.prepQuestionCount).isEqualTo(PREP_QUESTION_COUNT)
            assertThat(scope?.profileFactCount).isEqualTo(EXPECTED_PROFILE_FACTS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_deleteDismissed_closesTheDialogAndKeepsTheApplication() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.DeleteChosen)

            viewModel.onAction(ApplicationWorkspaceAction.DeleteDismissed)

            val state = current().ready()
            assertThat(state.isDeleteDialogVisible).isFalse()
            assertThat(state.notes).isEqualTo("Saved notes")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_deleteConfirmed_removesTheApplication() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.DeleteChosen)

            viewModel.onAction(ApplicationWorkspaceAction.DeleteConfirmed)

            assertThat(current()).isEqualTo(ApplicationDetailUiState.Deleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenNoProfileExists_countsNoProfileFactsAndGeneratesNoPrepQuestions() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            profileRepository.sendProfile(null)
            runCurrent()

            val scope = current().ready().deleteScope
            assertThat(scope?.profileFactCount).isEqualTo(0)
            assertThat(scope?.prepQuestionCount).isEqualTo(0)
            assertThat(scope?.hasCoverLetter).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun current(): ApplicationDetailUiState = viewModel.uiState.value

    private fun ApplicationDetailUiState.ready(): ApplicationDetailUiState.Ready =
        this as ApplicationDetailUiState.Ready

    private companion object {
        const val APPLICATION_ID = "application-1"
        const val PREP_QUESTION_COUNT = 6
        const val EXPECTED_PROFILE_FACTS = 18
    }
}
