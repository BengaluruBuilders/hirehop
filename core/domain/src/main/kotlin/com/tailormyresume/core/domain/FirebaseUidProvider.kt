package com.tailormyresume.core.domain

fun interface FirebaseUidProvider {
    fun uid(): String?
}
