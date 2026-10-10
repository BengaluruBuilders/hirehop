package com.tailormyresume.feature.profile.impl.experience

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

internal data class ExperienceRow(
    val entryId: String,
    val title: String,
    val company: String,
    val start: String,
    val end: String,
    val isCurrent: Boolean,
    val needsEndDate: Boolean,
)

internal sealed interface ExperienceUiState {
    data object Loading : ExperienceUiState

    data class Content(val rows: List<ExperienceRow>) : ExperienceUiState
}

@HiltViewModel
internal class ExperienceViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {
    val uiState: StateFlow<ExperienceUiState> = TODO()
}
