package com.hirehop.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class AppIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("X-App-Id", "hirehop").build())
}

internal class AuthInterceptor(private val tokens: IdTokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val first = chain.proceed(chain.request().withToken(forceRefresh = false))
        if (first.code != 401) return first
        val fresh = runBlocking { tokens.idToken(forceRefresh = true) } ?: return first
        first.close()
        return chain.proceed(chain.request().withBearer(fresh))
    }

    private fun Request.withToken(forceRefresh: Boolean): Request {
        val token = runBlocking { tokens.idToken(forceRefresh) } ?: return this
        return withBearer(token)
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()
}
