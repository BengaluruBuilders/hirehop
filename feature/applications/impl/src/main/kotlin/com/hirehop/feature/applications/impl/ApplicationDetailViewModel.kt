package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal const val NOTES_DEBOUNCE_MILLIS = 500L

@HiltViewModel(assistedFactory = ApplicationDetailViewModel.Factory::class)
class ApplicationDetailViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val draftNotes = MutableStateFlow<String?>(null)
    private val isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<ApplicationDetailUiState> = combine(
        applicationRepository.observeApplication(applicationId),
        draftNotes,
        isDeleted,
        ::toUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationDetailUiState.Loading,
    )

    init {
        persistNotesAfterPause()
    }

    fun updateStatus(status: ApplicationStatus) {
        viewModelScope.launch { applicationRepository.updateStatus(applicationId, status) }
    }

    fun updateNotes(notes: String) {
        draftNotes.value = notes
    }

    fun deleteApplication() {
        viewModelScope.launch {
            applicationRepository.deleteApplication(applicationId)
            isDeleted.value = true
        }
    }

    @OptIn(FlowPreview::class)
    private fun persistNotesAfterPause() {
        viewModelScope.launch {
            draftNotes
                .filterNotNull()
                .debounce(NOTES_DEBOUNCE_MILLIS)
                .collect { applicationRepository.updateNotes(applicationId, it) }
        }
    }

    private fun toUiState(
        application: JobApplication?,
        notesDraft: String?,
        deleted: Boolean,
    ): ApplicationDetailUiState = when {
        deleted -> ApplicationDetailUiState.Deleted
        application == null -> ApplicationDetailUiState.NotFound
        else -> ApplicationDetailUiState.Success(
            application = application,
            notes = notesDraft ?: application.notes,
            gapSummary = application.gapSummaryOrNull(),
            reviewProgress = application.reviewProgressOrNull(),
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): ApplicationDetailViewModel
    }
}

sealed interface ApplicationDetailUiState {
    data object Loading : ApplicationDetailUiState
    data object NotFound : ApplicationDetailUiState
    data object Deleted : ApplicationDetailUiState
    data class Success(
        val application: JobApplication,
        val notes: String,
        val gapSummary: GapSummary?,
        val reviewProgress: ReviewProgress?,
    ) : ApplicationDetailUiState
}
