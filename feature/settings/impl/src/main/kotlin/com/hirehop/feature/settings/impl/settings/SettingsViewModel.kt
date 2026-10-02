package com.hirehop.feature.settings.impl.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.settings.api.navigation.SettingsNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    paymentGateway: PaymentGateway,
    connectivityMonitor: ConnectivityMonitor,
    private val signInGateway: SignInGateway,
) : ViewModel() {

    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<SettingsUiState> = combine(
        sessionRepository.observeAccount(),
        sessionRepository.observeConsent(),
        paymentGateway.observeEntitlement(),
        connectivityMonitor.isOnline,
        local,
    ) { account, consent, entitlement, isOnline, localState ->
        SettingsUiState.Content(
            account = account,
            creditsLeft = entitlement.totalCredits,
            consentAcceptedAt = consent?.acceptedAt,
            isOffline = !isOnline || localState.forcedOffline,
            isSignOutConfirmVisible = localState.isSignOutConfirmVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = SettingsUiState.Loading,
    )

    fun onEnter(key: SettingsNavKey) {
        local.update { state -> state.copy(forcedOffline = key.scenario == DebugScenario.OFFLINE) }
    }

    fun onSignOutRequested() {
        val content = uiState.value as? SettingsUiState.Content ?: return
        if (content.isOffline) return
        local.update { state -> state.copy(isSignOutConfirmVisible = true) }
    }

    fun onSignOutDismissed() {
        local.update { state -> state.copy(isSignOutConfirmVisible = false) }
    }

    fun onSignOutConfirmed() {
        local.update { state -> state.copy(isSignOutConfirmVisible = false) }
        viewModelScope.launch { signInGateway.signOut() }
    }

    private data class LocalState(
        val forcedOffline: Boolean = false,
        val isSignOutConfirmVisible: Boolean = false,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
