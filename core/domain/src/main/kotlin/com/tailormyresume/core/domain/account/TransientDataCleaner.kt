package com.tailormyresume.core.domain.account

fun interface TransientDataCleaner {
    suspend fun clear()
}
