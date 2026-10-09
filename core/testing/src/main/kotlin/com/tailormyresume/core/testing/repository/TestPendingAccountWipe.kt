package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.PendingAccountWipe
import com.tailormyresume.core.data.repository.PendingWipeState

class TestPendingAccountWipe(var current: PendingWipeState = PendingWipeState.NONE) : PendingAccountWipe {
    val history = mutableListOf<PendingWipeState>()

    override suspend fun state(): PendingWipeState = current

    override suspend fun markRequested() = set(PendingWipeState.REQUESTED)

    override suspend fun markServerClosed() = set(PendingWipeState.SERVER_CLOSED)

    override suspend fun clear() = set(PendingWipeState.NONE)

    private fun set(next: PendingWipeState) {
        current = next
        history += next
    }
}
