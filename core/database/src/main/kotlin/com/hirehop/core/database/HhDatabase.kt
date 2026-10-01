package com.hirehop.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hirehop.core.database.dao.JobApplicationDao
import com.hirehop.core.database.dao.ProfileDao
import com.hirehop.core.database.model.JobApplicationEntity
import com.hirehop.core.database.model.ProfileEntity
import com.hirehop.core.database.model.ProfileEntryEntity
import com.hirehop.core.database.util.InstantConverter
import com.hirehop.core.database.util.JsonConverters

@Database(
    entities = [
        ProfileEntity::class,
        ProfileEntryEntity::class,
        JobApplicationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
    JsonConverters::class,
)
abstract class HhDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun jobApplicationDao(): JobApplicationDao
}
