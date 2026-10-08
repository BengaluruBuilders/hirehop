package com.tailormyresume.core.network

interface IdTokenProvider {
    fun idToken(forceRefresh: Boolean): String?
}

class SessionExpiredException : RuntimeException("Sign-in session expired")
