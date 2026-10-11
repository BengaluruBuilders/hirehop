package com.tailormyresume.app.account

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DataExportsStartTaskTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun startRemovesAStaleArchiveAndLeavesTheRestOfTheCache() = runTest(UnconfinedTestDispatcher()) {
        val archive = File(folder.newFolder("data-exports"), "tailormyresume-my-data.zip").apply { writeText("x") }
        val resume = File(folder.newFolder("exports"), "resume.pdf").apply { writeText("x") }

        DataExportsStartTask(File(folder.root, "data-exports"), UnconfinedTestDispatcher(testScheduler), TestScope(testScheduler)).start()
        testScheduler.advanceUntilIdle()

        assertThat(archive.exists()).isFalse()
        assertThat(resume.exists()).isTrue()
    }
}
