package com.tailormyresume.feature.onboarding.impl.upload

import androidx.lifecycle.ViewModel
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeFile
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeImportDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

internal enum class UploadEvent { OpenReading }

@HiltViewModel
internal class UploadViewModel @Inject constructor(
    private val draft: ResumeImportDraft,
) : ViewModel() {

    private val eventChannel = Channel<UploadEvent>(Channel.BUFFERED)

    val events: Flow<UploadEvent> = eventChannel.receiveAsFlow()

    fun onFilePicked(file: ResumeFile?) {
        if (file == null) return
        draft.setFile(file)
        eventChannel.trySend(UploadEvent.OpenReading)
    }
}
