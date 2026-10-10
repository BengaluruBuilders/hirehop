package com.tailormyresume.feature.tailor.impl.tailoring

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ApplicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal sealed interface TailoringOutcome {
    data object Done : TailoringOutcome

    data object Failed : TailoringOutcome
}

@HiltViewModel(assistedFactory = TailoringViewModel.Factory::class)
internal class TailoringViewModel @AssistedInject constructor(
    private val runner: TailoringRunner,
    private val applicationRepository: ApplicationRepository,
    @Assisted val applicationId: String,
) : ViewModel() {
    val progress: StateFlow<Int> = MutableStateFlow(0)

    val uiState: StateFlow<TailoringUiState> = MutableStateFlow(TailoringUiState(0, emptyList(), 0, emptyList()))

    val outcome: StateFlow<TailoringOutcome?> = MutableStateFlow(null)

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): TailoringViewModel
    }
}
