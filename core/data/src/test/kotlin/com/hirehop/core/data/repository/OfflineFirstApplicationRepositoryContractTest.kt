package com.hirehop.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hirehop.core.database.HhDatabase
import com.hirehop.core.database.createInMemoryHhDatabase
import com.hirehop.core.testing.repository.ApplicationRepositoryContractTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
class OfflineFirstApplicationRepositoryContractTest : ApplicationRepositoryContractTest() {

    private lateinit var database: HhDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = createInMemoryHhDatabase(context)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    override fun createApplicationRepository(): ApplicationRepository =
        OfflineFirstApplicationRepository(
            jobApplicationDao = database.jobApplicationDao(),
            clock = Clock.System,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
}
