package com.tailormyresume.core.domain.account

fun interface ServerAccountDeleter {
    val deletesRemoteData: Boolean get() = false

    suspend fun delete(): Result<Unit>
}
