package com.tailormyresume.core.data.repository

enum class PendingWipeState { NONE, REQUESTED, SERVER_CLOSED }

interface PendingAccountWipe {
    suspend fun state(): PendingWipeState

    suspend fun markRequested()

    suspend fun markRequested(uid: String)

    suspend fun markServerClosed()

    suspend fun uid(): String?

    suspend fun recordUid(uid: String)

    suspend fun promoteToServerClosed(uid: String): Boolean

    suspend fun clear()

    companion object {
        val None: PendingAccountWipe = object : PendingAccountWipe {
            override suspend fun state() = PendingWipeState.NONE

            override suspend fun markRequested() = Unit

            override suspend fun markRequested(uid: String) = Unit

            override suspend fun markServerClosed() = Unit

            override suspend fun uid(): String? = null

            override suspend fun recordUid(uid: String) = Unit

            override suspend fun promoteToServerClosed(uid: String) = false

            override suspend fun clear() = Unit
        }
    }
}
