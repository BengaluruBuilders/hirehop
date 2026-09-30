package com.hirehop.core.data.di

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.OfflineFirstApplicationRepository
import com.hirehop.core.data.repository.OfflineFirstProfileRepository
import com.hirehop.core.data.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsProfileRepository(
        profileRepository: OfflineFirstProfileRepository,
    ): ProfileRepository

    @Binds
    internal abstract fun bindsApplicationRepository(
        applicationRepository: OfflineFirstApplicationRepository,
    ): ApplicationRepository

    companion object {
        @Provides
        fun providesClock(): Clock = Clock.System
    }
}
