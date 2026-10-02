package com.hirehop.feature.tailor.impl.export.docx

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import com.hirehop.feature.tailor.impl.export.ExportDirectory
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile

class JdkDocxResumeRendererTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun render_writesANonEmptyFileWithTheGivenName() = runTest {
        val file = renderer().render(resume(), "resume.docx")

        assertThat(file.name).isEqualTo("resume.docx")
        assertThat(file.exists()).isTrue()
        assertThat(file.length()).isGreaterThan(0L)
    }

    @Test
    fun render_writesIntoTheExportDirectory() = runTest {
        val file = renderer().render(resume(), "resume.docx")

        assertThat(file.parentFile).isEqualTo(File(folder.root, "exports"))
    }

    @Test
    fun render_writesAReadableDocxPackage() = runTest {
        val file = renderer().render(resume(), "resume.docx")

        val names = ZipFile(file).use { zip -> zip.entries().toList().map { it.name } }

        assertThat(names).containsExactlyElementsIn(REQUIRED_PARTS).inOrder()
    }

    @Test
    fun render_producesByteIdenticalOutputForTheSameDocument() = runTest {
        val renderer = renderer()

        val first = renderer.render(resume(), "resume.docx").readBytes()
        val second = renderer.render(resume(), "resume.docx").readBytes()

        assertThat(second.contentEquals(first)).isTrue()
    }

    @Test
    fun render_replacesAnEarlierExportOfTheSameName() = runTest {
        val renderer = renderer()

        renderer.render(resume(), "resume.docx")
        renderer.render(resume(), "second.docx")

        assertThat(File(folder.root, "exports").list()?.toList()).containsExactly("second.docx")
    }

    @Test
    fun render_succeedsForALongBulletAnEmptySectionAndAMultiLineBullet() = runTest {
        val document = resume(
            sections = listOf(
                ResumeSection(EntryCategory.EXPERIENCE, "Experience", listOf(entry(listOf("Delivered ".repeat(500), "One\nTwo")))),
                ResumeSection(EntryCategory.PROJECT, "Projects", emptyList()),
            ),
        )

        val file = renderer().render(document, "resume.docx")

        assertThat(file.length()).isGreaterThan(0L)
        assertThat(runCatching { ZipFile(file) }.exceptionOrNull()).isNull()
    }

    @Test
    fun render_succeedsForAResumeWithNoSections() = runTest {
        val file = renderer().render(resume(sections = emptyList()), "resume.docx")

        assertThat(file.length()).isGreaterThan(0L)
    }

    private fun renderer(): JdkDocxResumeRenderer = JdkDocxResumeRenderer(
        exportDirectory = ExportDirectory(File(folder.root, "exports")),
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun resume(
        sections: List<ResumeSection> = listOf(
            ResumeSection(EntryCategory.EXPERIENCE, "Experience", listOf(entry())),
        ),
    ): ResumeDocument = ResumeDocument(
        name = "Ada Lovelace",
        contactLine = "ada@example.com",
        headline = "Staff Engineer",
        skills = listOf("Kotlin"),
        sections = sections,
        skillsHeading = "Skills",
    )

    private fun entry(bullets: List<String> = listOf("Cut release time in half")): ResumeEntry = ResumeEntry(
        title = "Staff Engineer",
        organization = "Analytical Engines",
        dateRange = "2020 - 2024",
        bullets = bullets,
    )

    private companion object {
        val REQUIRED_PARTS = listOf(
            "[Content_Types].xml",
            "_rels/.rels",
            "word/document.xml",
            "word/_rels/document.xml.rels",
            "docProps/core.xml",
            "docProps/app.xml",
        )
    }
}
