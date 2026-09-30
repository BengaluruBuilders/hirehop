package com.hirehop.core.domain.account

sealed interface AccountDeletionResult {

    data class Deleted(val counts: AccountDeletionCounts) : AccountDeletionResult

    data class Failed(val dataIntact: Boolean) : AccountDeletionResult
}
