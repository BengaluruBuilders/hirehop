package com.tailormyresume.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

internal class AppIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("X-App-Id", "tailormyresume").build())
}

internal class AuthInterceptor(
    private val tokens: IdTokenProvider,
    private val sessionListener: SessionExpiredListener,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = try {
            tokens.idToken(forceRefresh = false)
        } catch (expired: SessionExpiredException) {
            sessionListener.onSessionExpired()
            return unauthorised(chain.request())
        } ?: return chain.proceed(chain.request())
        val first = chain.proceed(chain.request().withBearer(token))
        if (first.code != 401) return first
        val fresh = try {
            tokens.idToken(forceRefresh = true)
        } catch (expired: SessionExpiredException) {
            sessionListener.onSessionExpired()
            null
        } catch (failure: IOException) {
            first.close()
            throw failure
        } ?: return first
        first.close()
        val second = chain.proceed(chain.request().withBearer(fresh))
        if (second.code == 401) sessionListener.onSessionExpired()
        return second
    }

    private fun unauthorised(request: Request): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(401)
        .message("Session expired")
        .header("Content-Type", "application/json")
        .body(EXPIRED_BODY.toResponseBody("application/json".toMediaType()))
        .build()

    private companion object {
        const val EXPIRED_BODY = """{"error":{"code":"UNAUTHENTICATED","message":"Session expired"}}"""
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
