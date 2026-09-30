package com.hirehop.core.database.di

import com.hirehop.core.database.HhDatabase
import com.hirehop.core.database.dao.JobApplicationDao
import com.hirehop.core.database.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides
    fun providesProfileDao(
        database: HhDatabase,
    ): ProfileDao = database.profileDao()

    @Provides
    fun providesJobApplicationDao(
        database: HhDatabase,
    ): JobApplicationDao = database.jobApplicationDao()
}
