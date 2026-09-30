package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.JobApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    applicationRepository: ApplicationRepository,
) : ViewModel() {

    val uiState: StateFlow<ApplicationsUiState> = applicationRepository
        .observeApplications()
        .map(::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApplicationsUiState.Loading,
        )

    private fun toUiState(applications: List<JobApplication>): ApplicationsUiState =
        if (applications.isEmpty()) {
            ApplicationsUiState.Empty
        } else {
            ApplicationsUiState.Success(applications.sortedByDescending { it.updatedAt })
        }
}

sealed interface ApplicationsUiState {
    data object Loading : ApplicationsUiState
    data object Empty : ApplicationsUiState
    data class Success(val items: List<JobApplication>) : ApplicationsUiState
}
