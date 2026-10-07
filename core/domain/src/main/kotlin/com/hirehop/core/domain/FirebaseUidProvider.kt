package com.hirehop.core.domain

fun interface FirebaseUidProvider {
    fun uid(): String?
}
