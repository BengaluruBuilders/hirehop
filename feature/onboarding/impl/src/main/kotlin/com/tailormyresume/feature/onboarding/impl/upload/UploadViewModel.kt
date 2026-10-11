package com.tailormyresume.feature.onboarding.impl.upload

import androidx.lifecycle.ViewModel
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeFile
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeImportDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

internal enum class UploadEvent { OpenReading }

@HiltViewModel
internal class UploadViewModel @Inject constructor(
    private val draft: ResumeImportDraft,
) : ViewModel() {

    val events: Flow<UploadEvent> = emptyFlow()

    fun onFilePicked(file: ResumeFile?) = Unit
}
