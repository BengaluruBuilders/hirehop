package com.hirehop.feature.tailor.impl.export

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class ExportDirectoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun exportDirectory(): File = File(folder.root, "exports")

    @Test
    fun write_createsTheFileWithTheContent() {
        val file = ExportDirectory(exportDirectory()).write("resume.pdf") { it.write("pdf".toByteArray()) }

        assertThat(file.name).isEqualTo("resume.pdf")
        assertThat(file.readText()).isEqualTo("pdf")
    }

    @Test
    fun write_removesOlderExports() {
        val directory = exportDirectory().also { it.mkdirs() }
        File(directory, "old.pdf").writeText("old")

        ExportDirectory(directory).write("new.pdf") { it.write(1) }

        assertThat(directory.list()?.toList()).containsExactly("new.pdf")
    }

    @Test
    fun write_replacesAFileWithTheSameName() {
        val directory = ExportDirectory(exportDirectory())
        directory.write("resume.pdf") { it.write("first".toByteArray()) }

        val file = directory.write("resume.pdf") { it.write("second".toByteArray()) }

        assertThat(file.readText()).isEqualTo("second")
    }

    @Test
    fun write_leavesNoPartialFileWhenTheWriterFails() {
        val directory = exportDirectory()

        val result = runCatching {
            ExportDirectory(directory).write("resume.pdf") {
                it.write("partial".toByteArray())
                throw IOException("disk full")
            }
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        assertThat(directory.list()?.toList()).isEmpty()
    }
}
