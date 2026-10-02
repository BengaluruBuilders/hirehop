package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.CreditKind
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.model.PrepPlanItem
import com.hirehop.core.model.ReportedItemKind
import com.hirehop.core.model.WrittenCoverLetter
import com.hirehop.core.model.WrittenParagraph
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.repository.TestContentReportRepository
import com.hirehop.core.testing.repository.TestCoverLetterRepository
import com.hirehop.core.testing.repository.TestExportHistoryRepository
import com.hirehop.core.testing.repository.TestPrepPlanRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class ApplicationDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = RecordingApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val exportHistoryRepository = TestExportHistoryRepository()
    private val paymentGateway = TestPaymentGateway().withFreeCredits(1)
    private val connectivityMonitor = TestConnectivityMonitor()
    private val prepPlanRepository = TestPrepPlanRepository()
    private val contentReportRepository = TestContentReportRepository()
    private val coverLetterRepository = TestCoverLetterRepository()
    private val prepQuestionSource = TestPrepQuestionSource(gapQuestions = 1) { _, _ -> PREP_QUESTION_COUNT }
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
        runTest {
            prepPlanRepository.add(APPLICATION_ID, PrepPlanItem(id = "d", text = "Requirement d"))
            prepPlanRepository.add(APPLICATION_ID, PrepPlanItem(id = "e", text = "Requirement e"))
        }
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    ApplicationDetailViewModel(
                        applicationRepository = applicationRepository,
                        profileRepository = profileRepository,
                        exportHistoryRepository = exportHistoryRepository,
                        prepQuestionSource = prepQuestionSource,
                        prepPlanRepository = prepPlanRepository,
                        contentReportRepository = contentReportRepository,
                        coverLetterRepository = coverLetterRepository,
                        clock = TestClock(),
                        paymentGateway = paymentGateway,
                        connectivityMonitor = connectivityMonitor,
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
    fun uiState_prepTasks_comeFromThePrepPlanInTheOrderTheyWereAdded() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            val tasks = current().ready().prepTasks
            assertThat(tasks.map { task -> task.id }).containsExactly("d", "e").inOrder()
            assertThat(tasks.map { task -> task.requirementText }).containsExactly("Requirement d", "Requirement e").inOrder()
            assertThat(tasks.none { task -> task.isDone }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenNothingWasAddedToThePrepPlan_hasNoPrepTasksEvenWithGaps() = runTest {
        prepPlanRepository.clearFor(APPLICATION_ID)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            assertThat(current().ready().prepTasks).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_anItemAddedLaterToThePrepPlanAppearsInTheWorkspace() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            prepPlanRepository.add(APPLICATION_ID, PrepPlanItem(id = "f", text = "Requirement f"))
            runCurrent()

            assertThat(current().ready().prepTasks.map { task -> task.id }).containsExactly("d", "e", "f").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheProfileExists_countsOnlyTheQuestionsTiedToFacts() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            profileRepository.sendProfile(canonicalCandidateProfile)
            runCurrent()

            assertThat(current().ready().prepQuestionCount).isEqualTo(PREP_QUESTION_COUNT)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_withNoStoredLetter_hasNoWrittenCoverLetter() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            profileRepository.sendProfile(canonicalCandidateProfile)
            runCurrent()

            assertThat(current().ready().coverLetter).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_withAStoredLetter_reportsItsWordCountAndDate() = runTest {
        val writtenAt = Instant.fromEpochMilliseconds(1_700_000_000_000)
        coverLetterRepository.save(
            APPLICATION_ID,
            WrittenCoverLetter(listOf(WrittenParagraph("One two three."), WrittenParagraph("Four five.")), writtenAt),
        )
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            profileRepository.sendProfile(canonicalCandidateProfile)
            runCurrent()

            assertThat(current().ready().coverLetter).isEqualTo(WorkspaceCoverLetter(wordCount = 5, writtenAt = writtenAt))
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
    fun uiState_whenAnExportExists_showsTheLastExportedFile() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            exportHistoryRepository.sendExports(
                listOf(
                    exportRecord(fileName = "first.pdf", format = ExportFormat.PDF, epochSeconds = 100),
                    exportRecord(fileName = "second.docx", format = ExportFormat.DOCX, epochSeconds = 200),
                ),
            )
            runCurrent()

            assertThat(current().ready().resume).isEqualTo(
                WorkspaceResume.Exported(
                    fileName = "second.docx",
                    format = ExportFormat.DOCX,
                    exportedAt = Instant.fromEpochSeconds(200),
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenAnotherApplicationWasExported_thisOneIsNotExported() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            exportHistoryRepository.sendExports(
                listOf(exportRecord(applicationId = "other", fileName = "other.pdf", epochSeconds = 100)),
            )
            runCurrent()

            assertThat(current().ready().resume).isEqualTo(WorkspaceResume.NotExported)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheDeviceGoesOffline_readsOffline() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            connectivityMonitor.setOnline(false)
            runCurrent()

            assertThat(current().ready().isOffline).isTrue()
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
    fun uiState_countsTheWordsOfTheJobDescription() = runTest {
        val withText = application.copy(
            job = application.job.copy(rawText = "Northwind GCC is\n hiring  today."),
        )

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(withText))
            runCurrent()

            assertThat(current().ready().jobDescriptionWordCount).isEqualTo(5)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_gapAnalysisToggled_expandsAndCollapsesTheMatchList() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            assertThat(current().ready().matches.map { match -> match.id })
                .containsExactly("a", "b", "c", "d", "e").inOrder()
            assertThat(current().ready().isGapExpanded).isFalse()

            viewModel.onAction(ApplicationWorkspaceAction.GapAnalysisToggled)
            assertThat(current().ready().isGapExpanded).isTrue()

            viewModel.onAction(ApplicationWorkspaceAction.GapAnalysisToggled)
            assertThat(current().ready().isGapExpanded).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_jobDescriptionToggled_again_collapsesTheJobDescription() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.JobDescriptionToggled)

            viewModel.onAction(ApplicationWorkspaceAction.JobDescriptionToggled)

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
            runCurrent()

            val tasks = current().ready().prepTasks
            assertThat(tasks.single { task -> task.isDone }.id).isEqualTo("d")
            assertThat(prepPlanRepository.observeItems(APPLICATION_ID).first().single { it.done }.id).isEqualTo("d")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskToggled_again_clearsTheTick() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskToggled("d"))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskToggled("d"))
            runCurrent()

            assertThat(current().ready().prepTasks.none { task -> task.isDone }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskInaccuracyReported_marksOnlyThatTaskAsReported() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskInaccuracyReported("d"))
            runCurrent()

            val tasks = current().ready().prepTasks
            assertThat(tasks.single { task -> task.isReported }.id).isEqualTo("d")
            val stored = contentReportRepository.observeReports(APPLICATION_ID).first().single()
            assertThat(stored.itemKind).isEqualTo(ReportedItemKind.REQUIREMENT)
            assertThat(stored.itemId).isEqualTo("d")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_prepTaskInaccuracyReported_sendsOneThankYouEvent() = runTest {
        viewModel.events.test {
            viewModel.onAction(ApplicationWorkspaceAction.PrepTaskInaccuracyReported("d"))

            assertThat(awaitItem()).isEqualTo(ApplicationDetailEvent.ReportRecorded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheExportHasPageCountAndTemplate_carriesThemToTheResumeCard() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            exportHistoryRepository.sendExports(
                listOf(exportRecord(fileName = "mine.pdf", epochSeconds = 100).copy(pageCount = 1, templateName = "Plain")),
            )
            runCurrent()

            val resume = current().ready().resume as WorkspaceResume.Exported
            assertThat(resume.pageCount).isEqualTo(1)
            assertThat(resume.templateName).isEqualTo("Plain")
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
    fun onAction_notesChanged_showsTheTypedTextBeforeAnySaveRuns() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.NotesChanged("abc"))
            runCurrent()

            assertThat(current().ready().notes).isEqualTo("abc")
            assertThat(applicationRepository.notesWrites).isEmpty()

            advanceTimeBy(1_000)
            runCurrent()

            assertThat(applicationRepository.notesWrites).containsExactly("abc")
            assertThat(current().ready().notes).isEqualTo("abc")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_notesChanged_whileASaveIsInFlight_isNotRevertedWhenThatSaveCompletes() = runTest {
        val gate = CompletableDeferred<Unit>()
        applicationRepository.notesWriteGate = gate
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.NotesChanged("abc"))
            advanceTimeBy(1_000)
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.NotesChanged("abcd"))
            runCurrent()
            gate.complete(Unit)
            runCurrent()

            assertThat(applicationRepository.notesWrites.first()).isEqualTo("abc")
            assertThat(current().ready().notes).isEqualTo("abcd")

            advanceTimeBy(1_000)
            runCurrent()

            assertThat(applicationRepository.notesWrites.last()).isEqualTo("abcd")
            assertThat(current().ready().notes).isEqualTo("abcd")
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
            assertThat(scope?.profileFactCount).isEqualTo(EXPECTED_PROFILE_FACTS)
            assertThat(scope?.creditCount).isEqualTo(1)
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
    fun onAction_deleteConfirmed_clearsTheExportHistoryOfThatApplicationOnly() = runTest {
        exportHistoryRepository.sendExports(
            listOf(
                exportRecord(applicationId = APPLICATION_ID, fileName = "mine.pdf", epochSeconds = 100),
                exportRecord(applicationId = "other", fileName = "other.pdf", epochSeconds = 200),
            ),
        )
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.DeleteConfirmed)
            runCurrent()

            exportHistoryRepository.observeExports().test {
                assertThat(awaitItem().map { record -> record.fileName }).containsExactly("other.pdf")
                cancelAndIgnoreRemainingEvents()
            }
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
            assertThat(current().ready().prepQuestionCount).isEqualTo(0)
            assertThat(current().ready().hasCoverLetter).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun exportRecord(
        fileName: String,
        applicationId: String = APPLICATION_ID,
        format: ExportFormat = ExportFormat.PDF,
        epochSeconds: Long,
    ) = ExportRecord(
        applicationId = applicationId,
        format = format,
        fileName = fileName,
        exportedAt = Instant.fromEpochSeconds(epochSeconds),
        creditKind = CreditKind.FREE,
    )

    private fun current(): ApplicationDetailUiState = viewModel.uiState.value

    private fun ApplicationDetailUiState.ready(): ApplicationDetailUiState.Ready =
        this as ApplicationDetailUiState.Ready

    private companion object {
        const val APPLICATION_ID = "application-1"
        const val PREP_QUESTION_COUNT = 6
        const val EXPECTED_PROFILE_FACTS = 27
    }
}
