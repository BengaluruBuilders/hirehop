package com.tailormyresume.feature.tailor.impl.result

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel(assistedFactory = TailoredViewModel.Factory::class)
internal class TailoredViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val assembler: ResumeDocumentAssembler,
    @Assisted val applicationId: String,
) : ViewModel() {
    val state: StateFlow<TailoredUiState> = MutableStateFlow(TailoredUiState.Loading)

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): TailoredViewModel
    }
}
