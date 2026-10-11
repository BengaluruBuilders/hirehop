package com.tailormyresume.feature.analysis.impl.result

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.CreditsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

@HiltViewModel(assistedFactory = JobResultViewModel.Factory::class)
internal class JobResultViewModel @AssistedInject constructor(
    applicationRepository: ApplicationRepository,
    creditsRepository: CreditsRepository,
    @Assisted val applicationId: String,
) : ViewModel() {

    val uiState: StateFlow<JobResultUiState> = MutableStateFlow(JobResultUiState.Loading)

    val events: Flow<JobResultEvent> = emptyFlow()

    fun onTailor() = Unit

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): JobResultViewModel
    }
}
