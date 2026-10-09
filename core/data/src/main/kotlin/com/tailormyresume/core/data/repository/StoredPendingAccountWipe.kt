package com.tailormyresume.core.data.repository

import com.tailormyresume.core.data.mock.MockStateStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoredPendingAccountWipe @Inject constructor(private val store: MockStateStore) : PendingAccountWipe {

    override suspend fun state(): PendingWipeState =
        PendingWipeState.entries.firstOrNull { it.name == store.read(KEY) } ?: PendingWipeState.NONE

    override suspend fun markRequested() = store.write(KEY, PendingWipeState.REQUESTED.name)

    override suspend fun markServerClosed() = store.write(KEY, PendingWipeState.SERVER_CLOSED.name)

    override suspend fun uid(): String? = null

    override suspend fun recordUid(uid: String) = Unit

    override suspend fun clear() = store.remove(KEY)

    private companion object {
        const val KEY = "account.pendingWipe"
    }
}
