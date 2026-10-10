package com.tailormyresume.feature.profile.impl.overview

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.model.ProfileEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.time.Clock

internal sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data object Empty : ProfileUiState

    data class Content(
        val initials: String,
        val name: String,
        val headline: String,
        val years: Int?,
        val city: String,
        val percent: Int,
        val experienceCount: Int,
        val educationCount: Int,
        val skillsCount: Int,
        val achievementsCount: Int,
        val linkedinMissing: Boolean,
        val sourceFileName: String?,
    ) : ProfileUiState
}

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = TODO()
}

internal fun yearsOfExperience(entries: List<ProfileEntry>, clock: Clock): Int? = TODO()
