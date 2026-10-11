package com.tailormyresume.feature.onboarding.impl.upload

import androidx.lifecycle.ViewModel
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeFile
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeImportDraft
import com.tailormyresume.feature.onboarding.impl.importresume.UploadFailure
import com.tailormyresume.feature.onboarding.impl.importresume.UploadFailureKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

internal enum class UnreadableEvent { OpenReading, OpenPaste }

@HiltViewModel
internal class UnreadableViewModel @Inject constructor(
    private val draft: ResumeImportDraft,
) : ViewModel() {

    val uiState: StateFlow<UploadFailure> = MutableStateFlow(UploadFailure(UploadFailureKind.Neutral))

    val events: Flow<UnreadableEvent> = emptyFlow()

    fun onFilePicked(file: ResumeFile?) = Unit

    fun onPasteAsText() = Unit
}
