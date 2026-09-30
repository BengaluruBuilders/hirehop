package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ApplicationDetailViewModel.Factory::class)
class ApplicationDetailViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    @ApplicationScope applicationScope: CoroutineScope,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val isDeleted = MutableStateFlow(false)

    private val notesAutosaver = NotesAutosaver(
        scope = viewModelScope,
        flushScope = applicationScope,
        save = { notes -> applicationRepository.updateNotes(applicationId, notes) },
    )

    val uiState: StateFlow<ApplicationDetailUiState> = combine(
        applicationRepository.observeApplication(applicationId),
        isDeleted,
        ::toUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationDetailUiState.Loading,
    )

    fun updateStatus(status: ApplicationStatus) {
        viewModelScope.launch { applicationRepository.updateStatus(applicationId, status) }
    }

    fun updateNotes(notes: String) {
        notesAutosaver.onNotesChanged(notes)
    }

    fun deleteApplication() {
        notesAutosaver.discard()
        viewModelScope.launch {
            applicationRepository.deleteApplication(applicationId)
            isDeleted.value = true
        }
    }

    override fun onCleared() {
        notesAutosaver.flush()
    }

    private fun toUiState(
        application: JobApplication?,
        deleted: Boolean,
    ): ApplicationDetailUiState = when {
        deleted -> ApplicationDetailUiState.Deleted
        application == null -> ApplicationDetailUiState.NotFound
        else -> ApplicationDetailUiState.Success(
            application = application,
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
        val gapSummary: GapSummary?,
        val reviewProgress: ReviewProgress?,
    ) : ApplicationDetailUiState
}
