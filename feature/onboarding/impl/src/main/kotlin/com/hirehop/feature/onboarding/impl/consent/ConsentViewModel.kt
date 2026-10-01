package com.hirehop.feature.onboarding.impl.consent

import androidx.lifecycle.ViewModel
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConsentViewModel @Inject constructor() : ViewModel() {

    private val mutableState = MutableStateFlow(ConsentUiState())

    private var hasEntered = false

    val uiState: StateFlow<ConsentUiState> = mutableState.asStateFlow()

    fun onEnter(key: ConsentNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = consentStateFor(key.scenario)
    }

    fun onAction(action: ConsentAction) {
        when (action) {
            is ConsentAction.PurposeToggled -> onPurposeToggled(action.purpose)
            ConsentAction.Agree -> onAgree()
            ConsentAction.NotNow -> onNotNow()
            ConsentAction.ReadAgain -> onReadAgain()
        }
    }

    private fun onPurposeToggled(purpose: ConsentPurpose) {
        mutableState.update { state ->
            state.copy(
                entries = state.entries.map { entry ->
                    if (entry.purpose == purpose) entry.copy(isAcknowledged = !entry.isAcknowledged) else entry
                },
            )
        }
    }

    private fun onAgree() {
        mutableState.update { state ->
            if (state.canAgree) {
                state.copy(
                    entries = state.entries.map { it.copy(isAcknowledged = true) },
                    isSaving = false,
                )
            } else {
                state
            }
        }
    }

    private fun onNotNow() {
        mutableState.update { it.copy(isDeclined = true) }
    }

    private fun onReadAgain() {
        mutableState.update { it.copy(isDeclined = false) }
    }
}
