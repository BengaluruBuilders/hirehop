package com.hirehop.app.network

import com.hirehop.app.BuildConfig
import com.hirehop.core.network.HirehopApiConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkConfigModule {
    @Provides
    fun apiConfig(): HirehopApiConfig = HirehopApiConfig(BuildConfig.HIREHOP_API_BASE_URL)
}
