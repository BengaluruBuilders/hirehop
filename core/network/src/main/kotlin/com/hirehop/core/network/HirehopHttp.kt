package com.hirehop.core.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

fun hirehopJson(): Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun hirehopOkHttpClient(tokens: IdTokenProvider): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .addInterceptor(AppIdInterceptor())
    .addInterceptor(AuthInterceptor(tokens))
    .build()

fun hirehopApi(config: HirehopApiConfig, client: OkHttpClient, json: Json): HirehopApi =
    Retrofit.Builder()
        .baseUrl(config.baseUrl.trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(HirehopApi::class.java)

private const val CONNECT_TIMEOUT_SECONDS = 15L
private const val READ_TIMEOUT_SECONDS = 60L
