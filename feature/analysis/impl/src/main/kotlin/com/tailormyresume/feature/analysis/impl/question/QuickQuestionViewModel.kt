package com.tailormyresume.feature.analysis.impl.question

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ApplicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

@HiltViewModel(assistedFactory = QuickQuestionViewModel.Factory::class)
internal class QuickQuestionViewModel @AssistedInject constructor(
    applicationRepository: ApplicationRepository,
    @Assisted val applicationId: String,
) : ViewModel() {

    val uiState: StateFlow<QuickQuestionUiState> = MutableStateFlow(QuickQuestionUiState.Loading)

    val events: Flow<QuickQuestionEvent> = emptyFlow()

    fun onPick(choice: QuickChoice) = Unit

    fun onDetailChange(text: String) = Unit

    fun onContinue() = Unit

    fun onSkip() = Unit

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): QuickQuestionViewModel
    }
}
