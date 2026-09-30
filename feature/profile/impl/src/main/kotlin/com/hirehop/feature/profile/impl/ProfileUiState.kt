package com.hirehop.feature.profile.impl

import com.hirehop.core.model.CandidateProfile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data object Empty : ProfileUiState

    data class Success(
        val profile: CandidateProfile,
        val unconfirmedCount: Int,
    ) : ProfileUiState
}
