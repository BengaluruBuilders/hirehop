package com.tailormyresume.feature.tailor.impl.edit

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.ResumeSettingsRepository
import com.tailormyresume.feature.tailor.impl.HandEditBulletUseCase
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.export.ResumePdfRenderer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel(assistedFactory = EditResumeViewModel.Factory::class)
internal class EditResumeViewModel @AssistedInject constructor(
    applicationRepository: ApplicationRepository,
    profileRepository: ProfileRepository,
    settingsRepository: ResumeSettingsRepository,
    assembler: ResumeDocumentAssembler,
    handEditBullet: HandEditBulletUseCase,
    renderer: ResumePdfRenderer,
    @Assisted val applicationId: String,
) : ViewModel() {

    val state: StateFlow<EditResumeUiState> get() = TODO()

    val events: Flow<EditResumeEvent> get() = TODO()

    fun onBulletChange(bulletId: String, text: String) { TODO() }

    fun onSave() { TODO() }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): EditResumeViewModel
    }
}
