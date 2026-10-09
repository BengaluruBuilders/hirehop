package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.PendingAccountWipe
import com.tailormyresume.core.data.repository.PendingWipeState
import kotlinx.coroutines.CompletableDeferred

class TestPendingAccountWipe(
    var current: PendingWipeState = PendingWipeState.NONE,
    var markerUid: String? = null,
) : PendingAccountWipe {
    val history = mutableListOf<PendingWipeState>()
    var stateGate: CompletableDeferred<Unit>? = null

    override suspend fun state(): PendingWipeState {
        stateGate?.await()
        return current
    }

    override suspend fun markRequested() {
        markerUid = null
        set(PendingWipeState.REQUESTED)
    }

    override suspend fun markRequested(uid: String) {
        markerUid = uid
        set(PendingWipeState.REQUESTED)
    }

    override suspend fun markServerClosed() = set(PendingWipeState.SERVER_CLOSED)

    override suspend fun uid(): String? = markerUid

    override suspend fun recordUid(uid: String) {
        markerUid = uid
    }

    override suspend fun promoteToServerClosed(uid: String): Boolean {
        if (current != PendingWipeState.REQUESTED || markerUid != uid) return false
        set(PendingWipeState.SERVER_CLOSED)
        return true
    }

    override suspend fun clear() {
        markerUid = null
        set(PendingWipeState.NONE)
    }

    private fun set(next: PendingWipeState) {
        current = next
        history += next
    }
}
