package com.tailormyresume.core.data.repository

enum class PendingWipeState { NONE, REQUESTED, SERVER_CLOSED }

interface PendingAccountWipe {
    suspend fun state(): PendingWipeState

    suspend fun markRequested()

    suspend fun markServerClosed()

    suspend fun clear()

    companion object {
        val None: PendingAccountWipe = object : PendingAccountWipe {
            override suspend fun state() = PendingWipeState.NONE

            override suspend fun markRequested() = Unit

            override suspend fun markServerClosed() = Unit

            override suspend fun clear() = Unit
        }
    }
}
