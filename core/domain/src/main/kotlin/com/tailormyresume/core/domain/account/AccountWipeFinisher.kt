package com.tailormyresume.core.domain.account

fun interface AccountWipeFinisher {
    suspend fun finish()

    companion object {
        val None = AccountWipeFinisher { }
    }
}
