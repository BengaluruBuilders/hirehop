package com.hirehop.core.data.mock

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.testing.mock.MockStateStoreContractTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DataStoreMockStateStoreTest : MockStateStoreContractTest() {

    @get:Rule
    val folder = TemporaryFolder()

    private var counter = 0

    override fun createStore(scope: CoroutineScope): MockStateStore = storeOn(scope, fileName())

    @Test
    fun valuesSurviveANewStoreOverTheSameFile() = runTest {
        val file = fileName()
        val firstScope = CoroutineScope(Job())
        storeOn(firstScope, file).write("key", "kept")
        firstScope.coroutineContext.job.cancelAndJoin()

        val reopened = DataStoreMockStateStore(
            PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { File(folder.root, file) }),
        )

        assertThat(reopened.read("key")).isEqualTo("kept")
    }

    private fun fileName(): String = "mock-${counter++}.preferences_pb"

    private fun storeOn(scope: CoroutineScope, file: String): MockStateStore = DataStoreMockStateStore(
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(folder.root, file) }),
    )
}
