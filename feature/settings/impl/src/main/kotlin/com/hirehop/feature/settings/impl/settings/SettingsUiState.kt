package com.hirehop.feature.settings.impl.settings

import com.hirehop.core.model.SignInAccount
import kotlin.time.Instant

enum class SettingsDestination { CREDITS_AND_HELP, YOUR_DATA, CONSENT_NOTICE, DELETE_ACCOUNT }

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Content(
        val account: SignInAccount?,
        val creditsLeft: Int,
        val consentAcceptedAt: Instant?,
        val isOffline: Boolean,
        val isSignOutConfirmVisible: Boolean,
    ) : SettingsUiState
}
