package com.tailormyresume.app.account

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ExportedFilesCleanerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun cleaner() = CacheExportedFiles(folder.root, UnconfinedTestDispatcher())

    private fun file(directory: String, name: String) =
        File(folder.newFolder(directory), name).apply { writeText("x") }

    @Test
    fun deleteAllRemovesBothDirectories() = runTest {
        val resume = file("exports", "resume.pdf")
        val archive = file("data-exports", "my-data.zip")

        cleaner().deleteAll()

        assertThat(resume.exists()).isFalse()
        assertThat(archive.exists()).isFalse()
        assertThat(File(folder.root, "exports").exists()).isFalse()
        assertThat(File(folder.root, "data-exports").exists()).isFalse()
    }

    @Test
    fun deleteAllLeavesOtherCacheFilesAlone() = runTest {
        file("exports", "resume.pdf")
        val other = File(folder.root, "keep.txt").apply { writeText("x") }

        cleaner().deleteAll()

        assertThat(other.exists()).isTrue()
    }

    @Test
    fun deleteAllWithNothingExportedIsHarmless() = runTest {
        cleaner().deleteAll()

        assertThat(folder.root.listFiles()).isEmpty()
    }
}
