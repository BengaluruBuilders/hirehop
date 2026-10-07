package com.hirehop.core.domain.account

fun interface ServerAccountDeleter {
    suspend fun delete(): Result<Unit>
}
