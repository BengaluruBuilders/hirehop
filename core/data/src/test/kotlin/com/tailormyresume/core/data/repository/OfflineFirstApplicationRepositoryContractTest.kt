package com.tailormyresume.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tailormyresume.core.database.TmrDatabase
import com.tailormyresume.core.database.createInMemoryTmrDatabase
import com.tailormyresume.core.testing.repository.ApplicationRepositoryContractTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
class OfflineFirstApplicationRepositoryContractTest : ApplicationRepositoryContractTest() {

    private lateinit var database: TmrDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = createInMemoryTmrDatabase(context)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    override fun createApplicationRepository(): ApplicationRepository =
        OfflineFirstApplicationRepository(
            jobApplicationDao = database.jobApplicationDao(),
            clock = Clock.System,
            cleanup = ApplicationCleanup {},
            ioDispatcher = UnconfinedTestDispatcher(),
        )
}
