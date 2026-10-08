package com.tailormyresume.core.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class AppIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("X-App-Id", "tailormyresume").build())
}

internal class AuthInterceptor(
    private val tokens: IdTokenProvider,
    private val sessionListener: SessionExpiredListener,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenOrNull(forceRefresh = false) ?: return chain.proceed(chain.request())
        val first = chain.proceed(chain.request().withBearer(token))
        if (first.code != 401) return first
        val fresh = tokenOrNull(forceRefresh = true) ?: return first
        first.close()
        val second = chain.proceed(chain.request().withBearer(fresh))
        if (second.code == 401) sessionListener.onSessionExpired()
        return second
    }

    private fun tokenOrNull(forceRefresh: Boolean): String? = try {
        tokens.idToken(forceRefresh)
    } catch (expired: SessionExpiredException) {
        sessionListener.onSessionExpired()
        null
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()
}

fun interface SessionExpiredListener {
    fun onSessionExpired()
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
