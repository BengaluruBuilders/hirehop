package com.hirehop.core.network

interface IdTokenProvider {
    suspend fun idToken(forceRefresh: Boolean): String?
}
