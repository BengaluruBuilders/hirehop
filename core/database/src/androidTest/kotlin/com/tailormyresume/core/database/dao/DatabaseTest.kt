package com.tailormyresume.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.tailormyresume.core.database.TmrDatabase
import org.junit.After
import org.junit.Before

internal abstract class DatabaseTest {

    private lateinit var db: TmrDatabase
    protected lateinit var profileDao: ProfileDao
    protected lateinit var jobApplicationDao: JobApplicationDao

    @Before
    fun setup() {
        db = run {
            val context = ApplicationProvider.getApplicationContext<Context>()
            Room.inMemoryDatabaseBuilder(
                context,
                TmrDatabase::class.java,
            ).build()
        }
        profileDao = db.profileDao()
        jobApplicationDao = db.jobApplicationDao()
    }

    @After
    fun teardown() = db.close()
}
