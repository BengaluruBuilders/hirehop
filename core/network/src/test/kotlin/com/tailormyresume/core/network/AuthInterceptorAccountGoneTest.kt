package com.tailormyresume.core.network

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

class AuthInterceptorAccountGoneTest {
    private val server = MockWebServer().apply { start() }
    private val goneFlags = mutableListOf<Boolean>()
    private var failOnFirstToken: SessionExpiredException? = null
    private var failOnRefresh: SessionExpiredException? = null

    private val tokens = object : IdTokenProvider {
        override fun idToken(forceRefresh: Boolean): String {
            if (forceRefresh) failOnRefresh?.let { throw it } else failOnFirstToken?.let { throw it }
            return "token"
        }
    }

    @After
    fun tearDown() = runCatching { server.shutdown() }.let { }

    private fun api(): TailorMyResumeApi = tailormyresumeApi(
        TailorMyResumeApiConfig(server.url("/").toString().trimEnd('/')),
        tailormyresumeOkHttpClient(tokens, sessionListener = { gone -> goneFlags += gone }),
        tailormyresumeJson(),
    )

    @Test
    fun interceptorPassesAccountGoneToListener() {
        failOnFirstToken = SessionExpiredException(accountGone = true)
        runBlocking { apiResult { api().me() } }

        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))
        failOnFirstToken = null
        failOnRefresh = SessionExpiredException(accountGone = true)
        runBlocking { apiResult { api().me() } }

        assertThat(goneFlags).containsExactly(true, true)
    }

    @Test
    fun plainExpiryPassesAccountGoneFalse() {
        failOnFirstToken = SessionExpiredException()
        runBlocking { apiResult { api().me() } }

        assertThat(goneFlags).containsExactly(false)
    }
}
