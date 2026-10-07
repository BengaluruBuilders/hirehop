package com.hirehop.core.network.di

import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.HirehopApiConfig
import com.hirehop.core.network.IdTokenProvider
import com.hirehop.core.network.hirehopApi
import com.hirehop.core.network.hirehopJson
import com.hirehop.core.network.hirehopOkHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun json(): Json = hirehopJson()

    @Provides
    @Singleton
    fun okHttpClient(tokens: IdTokenProvider): OkHttpClient = hirehopOkHttpClient(tokens)

    @Provides
    @Singleton
    fun api(config: HirehopApiConfig, client: OkHttpClient, json: Json): HirehopApi =
        hirehopApi(config, client, json)
}
