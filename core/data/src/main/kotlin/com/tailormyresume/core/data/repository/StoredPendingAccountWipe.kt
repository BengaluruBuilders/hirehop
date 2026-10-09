package com.tailormyresume.core.data.repository

import com.tailormyresume.core.data.mock.MockStateStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoredPendingAccountWipe @Inject constructor(private val store: MockStateStore) : PendingAccountWipe {

    override suspend fun state(): PendingWipeState =
        PendingWipeState.entries.firstOrNull { it.name == store.read(KEY) } ?: PendingWipeState.NONE

    override suspend fun markRequested() {
        store.remove(UID_KEY)
        store.write(KEY, PendingWipeState.REQUESTED.name)
    }

    override suspend fun markRequested(uid: String) = markRequested()

    override suspend fun markServerClosed() = store.write(KEY, PendingWipeState.SERVER_CLOSED.name)

    override suspend fun uid(): String? = store.read(UID_KEY)

    override suspend fun recordUid(uid: String) = store.write(UID_KEY, uid)

    override suspend fun promoteToServerClosed(uid: String): Boolean = false

    override suspend fun clear() {
        store.remove(KEY)
        store.remove(UID_KEY)
    }

    private companion object {
        const val KEY = "account.pendingWipe"
        const val UID_KEY = "account.pendingWipe.uid"
    }
}
