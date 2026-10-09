package com.tailormyresume.core.network

interface IdTokenProvider {
    fun idToken(forceRefresh: Boolean): String?
}

class SessionExpiredException(val accountGone: Boolean = false) : RuntimeException("Sign-in session expired")
