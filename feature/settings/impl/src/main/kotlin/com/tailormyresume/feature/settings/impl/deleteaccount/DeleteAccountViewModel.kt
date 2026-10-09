package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.account.AccountDeletionCounts
import com.tailormyresume.core.domain.account.AccountDeletionResult
import com.tailormyresume.core.domain.account.AccountDeletionStep
import com.tailormyresume.core.domain.account.DeleteAccountUseCase
import com.tailormyresume.core.domain.account.PendingWipeOutcome
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.PendingNavigation
import com.tailormyresume.feature.settings.api.navigation.AccountDeletedNavKey
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    connectivityMonitor: ConnectivityMonitor,
    private val sessionRepository: SessionRepository,
    private val deleteAccount: DeleteAccountUseCase,
) : ViewModel() {

    private val snapshot = MutableStateFlow<Snapshot?>(null)

    private val phase = MutableStateFlow(Phase())

    private var hasEntered = false

    val uiState: StateFlow<DeleteAccountUiState> = combine(
        snapshot,
        phase,
        connectivityMonitor.isOnline,
    ) { snapshot, phase, isOnline ->
        when {
            snapshot == null -> DeleteAccountUiState.Loading
            phase.stage == Stage.DELETING -> DeleteAccountUiState.Deleting(
                counts = snapshot.counts,
                accountEmail = snapshot.accountEmail,
                step = phase.step,
            )
            else -> DeleteAccountUiState.Ready(
                counts = snapshot.counts,
                accountEmail = snapshot.accountEmail,
                isOffline = !isOnline || phase.forcedOffline,
                failure = phase.failure,
                isConfirmVisible = phase.isConfirmVisible,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DeleteAccountUiState.Loading,
    )

    fun onEnter(key: DeleteAccountNavKey) {
        if (hasEntered) return
        hasEntered = true
        phase.value = when (key.scenario) {
            DebugScenario.OFFLINE -> Phase(forcedOffline = true)
            DebugScenario.DELETING -> Phase(stage = Stage.DELETING, step = AccountDeletionStep.DELETING_APPLICATIONS)
            DebugScenario.ERROR -> Phase(failure = DeleteAccountFailure.DATA_INTACT)
            else -> Phase()
        }
        viewModelScope.launch {
            snapshot.value = Snapshot(
                counts = deleteAccount.preview(),
                accountEmail = sessionRepository.observeAccount().first()?.email,
            )
            if (deleteAccount.hasServerClosedPendingWipe()) {
                phase.value = Phase(failure = DeleteAccountFailure.LOCAL_WIPE_PENDING)
                onFinishRemovalTapped()
            }
        }
    }

    fun onDeleteTapped() {
        val ready = uiState.value as? DeleteAccountUiState.Ready ?: return
        if (ready.isOffline) return
        viewModelScope.launch {
            snapshot.update { current -> current?.copy(counts = deleteAccount.preview()) }
            phase.update { current -> current.copy(isConfirmVisible = true) }
        }
    }

    fun onFinishRemovalTapped() {
        if (phase.value.failure != DeleteAccountFailure.LOCAL_WIPE_PENDING) return
        phase.value = Phase(stage = Stage.DELETING, step = AccountDeletionStep.CLOSING_ACCOUNT)
        PendingNavigation.set(listOf(AccountDeletedNavKey))
        viewModelScope.launch {
            when (deleteAccount.finishRemoval()) {
                PendingWipeOutcome.FINISHED, PendingWipeOutcome.NOTHING_PENDING -> Unit
                PendingWipeOutcome.ACCOUNT_KEPT -> {
                    PendingNavigation.consume()
                    phase.value = Phase()
                }
                PendingWipeOutcome.STILL_PENDING -> {
                    PendingNavigation.consume()
                    phase.value = Phase(failure = DeleteAccountFailure.LOCAL_WIPE_PENDING)
                }
            }
        }
    }

    fun onDeleteDismissed() {
        phase.update { current -> current.copy(isConfirmVisible = false) }
    }

    fun onDeleteConfirmed() {
        val ready = uiState.value as? DeleteAccountUiState.Ready ?: return
        if (!ready.isConfirmVisible) return
        if (ready.isOffline) {
            phase.update { current -> current.copy(isConfirmVisible = false) }
            return
        }
        phase.update { Phase(stage = Stage.DELETING, step = AccountDeletionStep.entries.first()) }
        PendingNavigation.set(listOf(AccountDeletedNavKey))
        viewModelScope.launch {
            when (val result = deleteAccount(onStep = ::onStep)) {
                is AccountDeletionResult.Deleted -> Unit
                is AccountDeletionResult.Failed -> {
                    PendingNavigation.consume()
                    phase.update { Phase(failure = result.toFailure()) }
                }
                AccountDeletionResult.LocalWipePending -> {
                    PendingNavigation.consume()
                    phase.update { Phase(failure = DeleteAccountFailure.LOCAL_WIPE_PENDING) }
                }
            }
        }
    }

    private fun AccountDeletionResult.Failed.toFailure(): DeleteAccountFailure =
        if (dataIntact) DeleteAccountFailure.DATA_INTACT else DeleteAccountFailure.PARTLY_DELETED

    private fun onStep(step: AccountDeletionStep) {
        phase.update { current -> current.copy(step = step) }
    }

    private enum class Stage { READY, DELETING }

    private data class Snapshot(
        val counts: AccountDeletionCounts,
        val accountEmail: String?,
    )

    private data class Phase(
        val stage: Stage = Stage.READY,
        val step: AccountDeletionStep = AccountDeletionStep.entries.first(),
        val failure: DeleteAccountFailure? = null,
        val forcedOffline: Boolean = false,
        val isConfirmVisible: Boolean = false,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
