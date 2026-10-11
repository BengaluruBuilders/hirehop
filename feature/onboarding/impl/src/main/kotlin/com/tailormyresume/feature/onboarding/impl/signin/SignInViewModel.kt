package com.tailormyresume.feature.onboarding.impl.signin

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.domain.SignInGateway
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val gateway: SignInGateway,
) : ViewModel() {

    private val state = MutableStateFlow(SignInUiState.Ready)

    val uiState: StateFlow<SignInUiState> = state.asStateFlow()

    fun onContinueWithGoogle() = Unit
}
