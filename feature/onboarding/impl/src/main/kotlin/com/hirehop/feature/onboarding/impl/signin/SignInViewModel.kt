package com.hirehop.feature.onboarding.impl.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInResult
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInGateway: SignInGateway,
) : ViewModel() {

    private val mutableState = MutableStateFlow(SignInUiState())

    private var hasEntered = false

    val uiState: StateFlow<SignInUiState> = mutableState.asStateFlow()

    fun onEnter(key: SignInNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = signInStateFor(key.scenario)
    }

    fun onAction(action: SignInAction) {
        when (action) {
            is SignInAction.AdultConfirmationChanged -> onAdultConfirmationChanged(action.isConfirmed)
            SignInAction.Continue -> onContinue()
            SignInAction.NotNow -> onNotNow()
            SignInAction.Revisit -> onRevisit()
            SignInAction.UnderEighteen -> onUnderEighteen()
            SignInAction.BackFromUnderEighteen -> onBackFromUnderEighteen()
        }
    }

    private fun onAdultConfirmationChanged(isConfirmed: Boolean) {
        mutableState.update { it.copy(isAdultConfirmed = isConfirmed, isAdultNudged = false, failure = null) }
    }

    private fun onContinue() {
        val state = mutableState.value
        if (state.stage != SignInStage.IDLE && state.stage != SignInStage.FAILED) return
        if (!state.isAdultConfirmed) {
            mutableState.value = state.copy(isAdultNudged = true)
            return
        }
        mutableState.value = state.copy(stage = SignInStage.IN_PROGRESS, failure = null)
        viewModelScope.launch {
            val result = signInGateway.signIn()
            mutableState.value = mutableState.value.settled(result)
        }
    }

    private fun onNotNow() {
        mutableState.update { it.copy(stage = SignInStage.SKIPPED) }
    }

    private fun onRevisit() {
        mutableState.update { it.copy(stage = SignInStage.IDLE, isAdultNudged = false) }
    }

    private fun onUnderEighteen() {
        mutableState.update { it.copy(stage = SignInStage.UNDER_18) }
    }

    private fun onBackFromUnderEighteen() {
        mutableState.update { it.copy(stage = SignInStage.IDLE, isAdultNudged = false) }
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
