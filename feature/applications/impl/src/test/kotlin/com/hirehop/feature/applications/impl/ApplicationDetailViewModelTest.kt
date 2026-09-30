package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.testing.util.MainDispatcherRule
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

class ApplicationDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = RecordingApplicationRepository()
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
                    ApplicationDetailViewModel(applicationRepository, applicationScope, APPLICATION_ID)
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
    fun uiState_whenApplicationExists_exposesSummaryAndProgress() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))

            val state = expectMostRecentItem() as ApplicationDetailUiState.Success
            assertThat(state.application).isEqualTo(application)
            assertThat(state.notes).isEqualTo("Saved notes")
            assertThat(state.gapSummary)
                .isEqualTo(GapSummary(met = 2, partial = 1, gap = 2, mustHaveGaps = listOf("Requirement d")))
            assertThat(state.reviewProgress).isEqualTo(ReviewProgress(reviewed = 2, total = 3))
        }
    }

    @Test
    fun uiState_whenApplicationHasNoAnalysis_hasNoSummaryOrProgress() = runTest {
        val bare = application.copy(gapAnalysis = null, tailoredResume = null)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(bare))

            val state = expectMostRecentItem() as ApplicationDetailUiState.Success
            assertThat(state.gapSummary).isNull()
            assertThat(state.reviewProgress).isNull()
        }
    }

    @Test
    fun uiState_whenApplicationIsMissing_isNotFound() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(emptyList())

            assertThat(expectMostRecentItem()).isEqualTo(ApplicationDetailUiState.NotFound)
        }
    }

    @Test
    fun updateStatus_storesNewStatus() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.updateStatus(ApplicationStatus.INTERVIEW)

        val stored = applicationRepository.observeApplication(APPLICATION_ID).first()
        assertThat(stored?.status).isEqualTo(ApplicationStatus.INTERVIEW)
    }

    @Test
    fun updateNotes_showsDraftImmediately() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.uiState.test {
            viewModel.updateNotes("Draft")

            val state = expectMostRecentItem() as ApplicationDetailUiState.Success
            assertThat(state.notes).isEqualTo("Draft")
        }
    }

    @Test
    fun updateNotes_doesNotSaveBeforeDebounceElapses() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.updateNotes("Draft")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS - 1)
        runCurrent()

        assertThat(applicationRepository.notesWrites).isEmpty()
    }

    @Test
    fun updateNotes_savesAfterDebounceElapses() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.updateNotes("Draft")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS)
        runCurrent()

        assertThat(applicationRepository.notesWrites).containsExactly("Draft")
        val stored = applicationRepository.observeApplication(APPLICATION_ID).first()
        assertThat(stored?.notes).isEqualTo("Draft")
    }

    @Test
    fun updateNotes_whenTypingContinues_savesOnlyTheLastValue() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.updateNotes("D")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS / 2)
        viewModel.updateNotes("Dr")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS / 2)
        viewModel.updateNotes("Dra")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS)
        runCurrent()

        assertThat(applicationRepository.notesWrites).containsExactly("Dra")
    }

    @Test
    fun onCleared_whenNotesAreNotSavedYet_savesLatestNotes() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.updateNotes("Draft")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS / 2)
        viewModelStore.clear()

        assertThat(applicationRepository.notesWrites).containsExactly("Draft")
        val stored = applicationRepository.observeApplication(APPLICATION_ID).first()
        assertThat(stored?.notes).isEqualTo("Draft")
    }

    @Test
    fun onCleared_whenNotesAreAlreadySaved_doesNotSaveAgain() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.updateNotes("Draft")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS)
        runCurrent()
        viewModelStore.clear()

        assertThat(applicationRepository.notesWrites).containsExactly("Draft")
    }

    @Test
    fun onCleared_whenNotesWereNeverEdited_savesNothing() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModelStore.clear()

        assertThat(applicationRepository.notesWrites).isEmpty()
    }

    @Test
    fun deleteApplication_removesItAndEmitsDeleted() = runTest {
        applicationRepository.sendApplications(listOf(application))

        viewModel.uiState.test {
            viewModel.deleteApplication()

            assertThat(expectMostRecentItem()).isEqualTo(ApplicationDetailUiState.Deleted)
        }
        val remaining = applicationRepository.observeApplications().first()
        assertThat(remaining).isEmpty()
    }

    private companion object {
        const val APPLICATION_ID = "application-1"
    }
}
