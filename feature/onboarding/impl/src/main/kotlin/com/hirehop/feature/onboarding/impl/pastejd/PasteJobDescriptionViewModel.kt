package com.hirehop.feature.onboarding.impl.pastejd

import androidx.lifecycle.ViewModel
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PasteJobDescriptionViewModel @Inject constructor() : ViewModel() {

    private val mutableState = MutableStateFlow(PasteJobDescriptionUiState())

    private var hasEntered = false

    val uiState: StateFlow<PasteJobDescriptionUiState> = mutableState.asStateFlow()

    fun onEnter(
        key: PasteJobDescriptionNavKey,
        sharedText: String = "",
    ) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = pasteJobDescriptionStateFor(
            scenario = key.scenario,
            sharedText = sharedText,
        )
    }

    fun onAction(action: PasteJobDescriptionAction) {
        when (action) {
            is PasteJobDescriptionAction.TextChanged -> onTextChanged(action.value)
            is PasteJobDescriptionAction.CompanyChanged -> onCompanyChanged(action.value)
            is PasteJobDescriptionAction.RoleChanged -> onRoleChanged(action.value)
            PasteJobDescriptionAction.ClearTapped -> onClear()
            PasteJobDescriptionAction.AnalyseTapped -> onAnalyse()
            PasteJobDescriptionAction.RetryTapped -> mutableState.update { it.copy(message = null) }
            PasteJobDescriptionAction.DismissMessageTapped -> mutableState.update { it.copy(message = null) }
            PasteJobDescriptionAction.AnalysisRequestConsumed -> {
                mutableState.update { it.copy(analysisRequest = null) }
            }
        }
    }

    private fun onTextChanged(value: String) {
        mutableState.update { state ->
            state.copy(text = value, message = null, analysisRequest = null)
        }
    }

    private fun onCompanyChanged(value: String) {
        mutableState.update { it.copy(company = value) }
    }

    private fun onRoleChanged(value: String) {
        mutableState.update { it.copy(role = value) }
    }

    private fun onClear() {
        mutableState.update { state ->
            if (state.canClear) {
                state.copy(
                    text = "",
                    company = "",
                    role = "",
                    message = null,
                    analysisRequest = null,
                )
            } else {
                state
            }
        }
    }

    private fun onAnalyse() {
        mutableState.update { state ->
            if (state.canAnalyse) {
                state.copy(
                    analysisRequest = PasteJobDescriptionHandoff(
                        text = state.text.trim(),
                        company = state.company.trim(),
                        role = state.role.trim(),
                    ),
                )
            } else {
                state
            }
        }
    }
}
