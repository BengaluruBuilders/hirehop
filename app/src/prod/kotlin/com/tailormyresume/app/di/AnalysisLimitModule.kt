package com.tailormyresume.app.di

import com.tailormyresume.core.data.repository.AnalysisLimitPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AnalysisLimitModule {
    @Provides
    fun provideAnalysisLimitPolicy(): AnalysisLimitPolicy = object : AnalysisLimitPolicy {
        override val isCountedOnDevice = false
    }
}
