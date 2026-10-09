package com.tailormyresume.feature.settings.impl.deleteaccount

import com.tailormyresume.core.domain.account.AccountDeletionCounts
import com.tailormyresume.core.domain.account.AccountDeletionStep

enum class DeleteAccountFailure { DATA_INTACT, PARTLY_DELETED, LOCAL_WIPE_PENDING, CLOSE_UNCONFIRMED }

internal fun DeleteAccountFailure?.isWipePending(): Boolean =
    this == DeleteAccountFailure.LOCAL_WIPE_PENDING || this == DeleteAccountFailure.CLOSE_UNCONFIRMED

sealed interface DeleteAccountUiState {
    data object Loading : DeleteAccountUiState

    data class Ready(
        val counts: AccountDeletionCounts,
        val accountEmail: String?,
        val isOffline: Boolean,
        val failure: DeleteAccountFailure?,
        val isConfirmVisible: Boolean,
    ) : DeleteAccountUiState

    data class Deleting(
        val counts: AccountDeletionCounts,
        val accountEmail: String?,
        val step: AccountDeletionStep,
    ) : DeleteAccountUiState
}
