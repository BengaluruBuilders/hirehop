package com.hirehop.app.account

import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.HirehopApiConfig
import com.hirehop.core.network.IdTokenProvider
import com.hirehop.core.network.hirehopApi
import com.hirehop.core.network.hirehopJson
import com.hirehop.core.network.hirehopOkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

internal fun MockWebServer.api(): HirehopApi = hirehopApi(
    HirehopApiConfig(url("/").toString().trimEnd('/')),
    hirehopOkHttpClient(object : IdTokenProvider {
        override fun idToken(forceRefresh: Boolean) = "token"
    }),
    hirehopJson(),
)

internal fun jsonResponse(code: Int, body: String) =
    MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)

internal fun errorResponse(code: Int, errorCode: String) =
    jsonResponse(code, """{"error":{"code":"$errorCode","message":"m"}}""")
