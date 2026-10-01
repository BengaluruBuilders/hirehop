package com.hirehop.feature.settings.impl.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.SignInGateway
import com.hirehop.feature.settings.api.navigation.SettingsNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val paymentGateway: PaymentGateway,
    private val signInGateway: SignInGateway,
) : ViewModel() {

    private val mutableState = MutableStateFlow(SettingsUiState())

    private var hasEntered = false

    private var isOffline = false

    val uiState: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    fun onEnter(key: SettingsNavKey) {
        if (hasEntered) return
        hasEntered = true
        isOffline = settingsIsOffline(key.scenario)
        viewModelScope.launch { load() }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.DestinationSelected -> onDestinationSelected(action.destination)
            SettingsAction.DestinationConsumed -> mutableState.update { it.copy(destination = null) }
        }
    }

    private suspend fun load() {
        val account = runCatching { signInGateway.currentAccount() }.getOrNull()
        val entitlement = runCatching { paymentGateway.entitlement() }.getOrNull()
        mutableState.value = settingsStateFor(
            accountDisplayName = account?.displayName,
            creditsLeft = entitlement?.totalCredits ?: 0,
            isOffline = isOffline,
        )
    }

    private fun onDestinationSelected(destination: SettingsDestination) {
        mutableState.update { state ->
            val row = state.groups
                .flatMap { group -> group.rows }
                .firstOrNull { candidate -> candidate.destination == destination }
            if (row == null || !row.isEnabled) {
                state
            } else {
                state.copy(destination = destination)
            }
        }
    }
}
