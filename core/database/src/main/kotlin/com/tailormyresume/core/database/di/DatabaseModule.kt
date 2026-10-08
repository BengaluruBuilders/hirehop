package com.tailormyresume.core.database.di

import android.content.Context
import androidx.room.Room
import com.tailormyresume.core.database.TmrDatabase
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
    fun providesTmrDatabase(
        @ApplicationContext context: Context,
    ): TmrDatabase = Room.databaseBuilder(
        context,
        TmrDatabase::class.java,
        "hh-database",
    ).build()
}
