package com.hirehop.feature.onboarding.impl.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.DiscardJobDraftsUseCase
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInResult
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import com.hirehop.feature.onboarding.impl.common.observeOffline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInGateway: SignInGateway,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
    private val sessionRepository: SessionRepository,
    private val discardJobDrafts: DiscardJobDraftsUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow(SignInUiState())

    private var hasEntered = false

    val uiState: StateFlow<SignInUiState> = mutableState.asStateFlow()

    fun onEnter(key: SignInNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = signInStateFor(key.scenario)
        val forcedOffline = key.scenario == DebugScenario.OFFLINE
        viewModelScope.launch {
            connectivityMonitor.observeOffline(forcedOffline).collect { offline ->
                mutableState.update { it.copy(isOffline = offline) }
            }
        }
    }

    fun onAction(action: SignInAction) {
        when (action) {
            is SignInAction.AdultConfirmationChanged -> onAdultConfirmationChanged(action.isConfirmed)
            SignInAction.Continue -> onContinue()
            SignInAction.UnderEighteen -> onUnderEighteen()
            SignInAction.BackFromUnderEighteen -> {
                mutableState.update { it.copy(stage = SignInStage.IDLE, isAdultNudged = false) }
            }

            SignInAction.NextStepConsumed -> mutableState.update { it.copy(nextStep = null) }
        }
    }

    private fun onUnderEighteen() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                sessionRepository.observeKeptJobDescription().first()?.let { discardJobDrafts(it) }
                sessionRepository.clearKeptJobDescription()
                sessionRepository.clearCareerStage()
            }
            mutableState.update { it.copy(stage = SignInStage.UNDER_18) }
        }
    }

    private fun onAdultConfirmationChanged(isConfirmed: Boolean) {
        mutableState.update { it.copy(isAdultConfirmed = isConfirmed, isAdultNudged = false, failure = null) }
    }

    private fun onContinue() {
        val state = mutableState.value
        if (state.isBusy || state.stage == SignInStage.UNDER_18 || state.isOffline) return
        if (!state.isAdultConfirmed) {
            mutableState.value = state.copy(isAdultNudged = true)
            return
        }
        mutableState.value = state.copy(stage = SignInStage.IN_PROGRESS, failure = null)
        viewModelScope.launch {
            val result = signInGateway.signIn()
            val settled = mutableState.value.settled(result)
            mutableState.value = if (result is SignInResult.SignedIn) {
                settled.copy(nextStep = nextOnboardingStep())
            } else {
                settled
            }
        }
    }

    private fun SignInUiState.settled(result: SignInResult): SignInUiState = when (result) {
        is SignInResult.SignedIn -> copy(
            stage = SignInStage.SIGNED_IN,
            failure = null,
            displayName = result.account.displayName,
        )

        SignInResult.Cancelled -> copy(stage = SignInStage.CANCELLED, failure = null)

        is SignInResult.Failed -> copy(
            stage = SignInStage.FAILED,
            failure = result.reason,
            displayName = null,
        )
    }
}
