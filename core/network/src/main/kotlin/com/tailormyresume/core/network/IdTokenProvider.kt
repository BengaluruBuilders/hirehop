package com.tailormyresume.core.network

interface IdTokenProvider {
    fun idToken(forceRefresh: Boolean): String?
}
