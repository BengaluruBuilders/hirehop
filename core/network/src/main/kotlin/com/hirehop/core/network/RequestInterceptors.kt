package com.hirehop.core.network

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
        val fresh = tokens.idToken(forceRefresh = true) ?: return first
        first.close()
        return chain.proceed(chain.request().withBearer(fresh))
    }

    private fun Request.withToken(forceRefresh: Boolean): Request {
        val token = tokens.idToken(forceRefresh) ?: return this
        return withBearer(token)
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()
}

fun interface ConsentRequiredListener {
    fun onConsentRequired()
}

internal class ConsentRequiredInterceptor(private val listener: ConsentRequiredListener) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == HTTP_FORBIDDEN && response.carriesConsentRequired()) listener.onConsentRequired()
        return response
    }

    private fun Response.carriesConsentRequired(): Boolean = runCatching {
        errorJson.decodeFromString<ErrorEnvelope>(peekBody(PEEK_LIMIT_BYTES).string()).error?.code == "CONSENT_REQUIRED"
    }.getOrDefault(false)

    private companion object {
        const val HTTP_FORBIDDEN = 403
        const val PEEK_LIMIT_BYTES = 4096L
    }
}
