package com.tailormyresume.core.data.repository

import com.tailormyresume.core.data.mock.MockStateStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoredPendingAccountWipe @Inject constructor(private val store: MockStateStore) : PendingAccountWipe {
    private val mutex = Mutex()

    override suspend fun state(): PendingWipeState = stateOf(store.read(KEY))

    override suspend fun markRequested() = mutex.withLock {
        store.remove(UID_KEY)
        store.write(KEY, PendingWipeState.REQUESTED.name)
    }

    override suspend fun markRequested(uid: String) = mutex.withLock {
        store.remove(UID_KEY)
        store.write(KEY, encode(PendingWipeState.REQUESTED, uid))
    }

    override suspend fun markServerClosed() = mutex.withLock {
        store.write(KEY, encode(PendingWipeState.SERVER_CLOSED, uid()))
    }

    override suspend fun uid(): String? =
        store.read(KEY)?.substringAfter(SEPARATOR, "")?.takeIf { it.isNotEmpty() } ?: store.read(UID_KEY)

    override suspend fun recordUid(uid: String) = mutex.withLock {
        val raw = store.read(KEY)
        if (raw == null) store.write(UID_KEY, uid) else store.write(KEY, encode(stateOf(raw), uid))
    }

    override suspend fun promoteToServerClosed(uid: String): Boolean = mutex.withLock {
        val promotable = state() == PendingWipeState.REQUESTED && uid() == uid
        if (promotable) store.write(KEY, encode(PendingWipeState.SERVER_CLOSED, uid))
        promotable
    }

    override suspend fun clear() = mutex.withLock {
        store.remove(KEY)
        store.remove(UID_KEY)
    }

    private fun stateOf(raw: String?): PendingWipeState =
        PendingWipeState.entries.firstOrNull { it.name == raw?.substringBefore(SEPARATOR) } ?: PendingWipeState.NONE

    private fun encode(state: PendingWipeState, uid: String?): String =
        if (uid == null) state.name else "${state.name}$SEPARATOR$uid"

    private companion object {
        const val KEY = "account.pendingWipe"
        const val UID_KEY = "account.pendingWipe.uid"
        const val SEPARATOR = "|"
    }
}
