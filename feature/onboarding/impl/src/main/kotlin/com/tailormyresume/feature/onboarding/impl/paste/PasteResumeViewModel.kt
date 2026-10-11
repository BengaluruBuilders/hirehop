package com.tailormyresume.feature.onboarding.impl.paste

import androidx.lifecycle.ViewModel
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeImportDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

internal data class PasteResumeUiState(val text: String = "", val canRead: Boolean = false)

internal enum class PasteResumeEvent { OpenReading }

@HiltViewModel
internal class PasteResumeViewModel @Inject constructor(
    private val draft: ResumeImportDraft,
) : ViewModel() {

    val uiState: StateFlow<PasteResumeUiState> = MutableStateFlow(PasteResumeUiState())

    val events: Flow<PasteResumeEvent> = emptyFlow()

    fun onTextChange(text: String) = Unit

    fun onRead() = Unit
}
