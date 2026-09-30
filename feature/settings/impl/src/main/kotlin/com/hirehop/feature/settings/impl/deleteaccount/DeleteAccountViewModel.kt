package com.hirehop.feature.settings.impl.deleteaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.account.AccountDeletionCounts
import com.hirehop.core.domain.account.AccountDeletionResult
import com.hirehop.core.domain.account.AccountDeletionStep
import com.hirehop.core.domain.account.DeleteAccountUseCase
import com.hirehop.feature.settings.api.navigation.DeleteAccountNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val deleteAccount: DeleteAccountUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow(DeleteAccountUiState())

    private var hasEntered = false

    val uiState: StateFlow<DeleteAccountUiState> = mutableState.asStateFlow()

    fun onEnter(key: DeleteAccountNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = deleteAccountStateFor(
            scenario = key.scenario,
            counts = AccountDeletionCounts(profileFacts = 0, applications = 0, unusedCredits = 0),
            isOffline = deleteAccountIsOffline(key.scenario),
        )
        viewModelScope.launch {
            val counts = loadCounts()
            mutableState.update { state ->
                if (state.stage == DeleteAccountStage.DEFAULT) state.copy(counts = counts) else state
            }
        }
    }

    fun onAction(action: DeleteAccountAction) {
        when (action) {
            DeleteAccountAction.KeepAccountTapped -> Unit
            DeleteAccountAction.DeleteAccountTapped -> onDeleteAccountTapped()
            DeleteAccountAction.DownloadDataTapped -> select(DeleteAccountDestination.YOUR_DATA)
            DeleteAccountAction.BackToWelcomeTapped -> select(DeleteAccountDestination.WELCOME)
            DeleteAccountAction.DestinationConsumed -> mutableState.update { it.copy(destination = null) }
        }
    }

    private fun onDeleteAccountTapped() {
        if (!mutableState.value.isDeleteEnabled) return
        mutableState.update { state ->
            state.copy(
                stage = DeleteAccountStage.DELETING,
                steps = deleteAccountSteps(currentIndex = 0),
            )
        }
        viewModelScope.launch {
            when (val result = deleteAccount(onStep = ::onStep)) {
                is AccountDeletionResult.Deleted -> mutableState.update { state ->
                    state.copy(
                        stage = DeleteAccountStage.DONE,
                        counts = result.counts,
                        steps = deleteAccountSteps(currentIndex = AccountDeletionStep.entries.size),
                    )
                }

                is AccountDeletionResult.Failed -> mutableState.update { state ->
                    state.copy(
                        stage = DeleteAccountStage.ERROR,
                        isDataIntact = result.dataIntact,
                        steps = deleteAccountSteps(currentIndex = 0),
                    )
                }
            }
        }
    }

    private fun onStep(step: AccountDeletionStep) {
        mutableState.update { state ->
            state.copy(
                steps = deleteAccountSteps(currentIndex = step.ordinal),
            )
        }
    }

    private fun select(destination: DeleteAccountDestination) {
        mutableState.update { it.copy(destination = destination) }
    }

    private suspend fun loadCounts(): AccountDeletionCounts =
        runCatching { deleteAccount.preview() }
            .getOrDefault(AccountDeletionCounts(profileFacts = 0, applications = 0, unusedCredits = 0))
}
