package com.hirehop.core.database.di

import android.content.Context
import androidx.room.Room
import com.hirehop.core.database.HhDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesHhDatabase(
        @ApplicationContext context: Context,
    ): HhDatabase = Room.databaseBuilder(
        context,
        HhDatabase::class.java,
        "hh-database",
    ).build()
}
