package com.hirehop.core.network

interface IdTokenProvider {
    fun idToken(forceRefresh: Boolean): String?
}
