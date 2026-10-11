package com.tailormyresume.feature.analysis.impl.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.domain.coverage.KeywordCoverageCalculator
import com.tailormyresume.core.model.QuickAnswer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = QuickQuestionViewModel.Factory::class)
internal class QuickQuestionViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val picked = MutableStateFlow<QuickChoice?>(null)
    private val detail = MutableStateFlow("")
    private val eventChannel = Channel<QuickQuestionEvent>(Channel.BUFFERED)

    val uiState: StateFlow<QuickQuestionUiState> = combine(
        applicationRepository.observeApplication(applicationId),
        picked,
        detail,
    ) { application, choice, currentDetail ->
        val question = application?.gapAnalysis?.question
        if (question == null) {
            QuickQuestionUiState.NoQuestion
        } else {
            QuickQuestionUiState.Ready(question.text, question.why, choice, currentDetail)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, QuickQuestionUiState.Loading)

    val events: Flow<QuickQuestionEvent> = eventChannel.receiveAsFlow()

    fun onPick(choice: QuickChoice) {
        picked.value = choice
        if (!choice.takesDetail) detail.value = ""
    }

    fun onDetailChange(text: String) {
        if (picked.value?.takesDetail != true) return
        detail.value = text.take(QUICK_DETAIL_MAX)
    }

    fun onContinue() {
        val choice = picked.value
        if (choice == null) {
            viewModelScope.launch { eventChannel.send(QuickQuestionEvent.ToastPickAnswer) }
            return
        }
        val answerDetail = if (choice.takesDetail) detail.value.trim() else ""
        viewModelScope.launch {
            if (save(choice, answerDetail)) eventChannel.send(QuickQuestionEvent.Tailor(applicationId))
        }
    }

    fun onSkip() {
        viewModelScope.launch {
            save(null, "")
            eventChannel.send(QuickQuestionEvent.Tailor(applicationId))
        }
    }

    private suspend fun save(choice: QuickChoice?, answerDetail: String): Boolean {
        val current = applicationRepository.observeApplication(applicationId).first() ?: return false
        val gap = current.gapAnalysis ?: return false
        val requirementId = gap.question?.requirementId
        val answer = if (choice != null && requirementId != null) {
            QuickAnswer(requirementId, choice.name, answerDetail)
        } else {
            null
        }
        applicationRepository.upsertApplication(
            current.copy(
                quickAnswer = answer,
                keywordCoverage = KeywordCoverageCalculator.compute(gap.matches, answer, null),
            ),
        )
        return true
    }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): QuickQuestionViewModel
    }
}
