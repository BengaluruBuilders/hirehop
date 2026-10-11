package com.tailormyresume.feature.tailor.impl.export

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.PageSize
import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import com.tailormyresume.feature.tailor.impl.document.ResumeEntry
import com.tailormyresume.feature.tailor.impl.document.ResumeSection
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidPdfResumeRendererTest {

    private val renderer = AndroidPdfResumeRenderer(
        ApplicationProvider.getApplicationContext(),
        UnconfinedTestDispatcher(),
        UnconfinedTestDispatcher(),
    )

    private val document = ResumeDocument(
        name = "Priya Deshmukh",
        contactLine = "priya.deshmukh@gmail.com | +91 98200 41736",
        headline = "Finance analyst",
        skills = listOf("SQL", "Power BI"),
        sections = listOf(
            ResumeSection(
                category = EntryCategory.EXPERIENCE,
                heading = "Experience",
                entries = listOf(
                    ResumeEntry(
                        title = "Business Analyst",
                        organization = "Infosys",
                        dateRange = "Jul 2022 - Present",
                        bullets = listOf("Built Power BI dashboards for monthly financial reporting."),
                    ),
                ),
            ),
        ),
        skillsHeading = "Skills",
        summary = "Finance analyst with 4 years of SQL, Power BI and Excel work.",
    )

    private suspend fun pdfText(size: PageSize): String =
        renderer.render(document, "resume.pdf", size).file.readBytes().toString(Charsets.ISO_8859_1)

    @Test
    fun writesSelectableTextPdfAtPageSize() = runTest {
        val a4 = pdfText(PageSize.A4)
        val letter = pdfText(PageSize.LETTER)

        assertThat(a4).startsWith("%PDF")
        assertThat(a4).contains("/MediaBox [0 0 595 842]")
        assertThat(letter).contains("/MediaBox [0 0 612 792]")
        assertThat(a4).contains("/ToUnicode")
    }

    @Test
    fun theRenderedFileLivesInTheExportsDirectory() = runTest {
        val rendered = renderer.render(document, "Priya-Deshmukh_Resume.pdf", PageSize.A4)

        assertThat(rendered.file.parentFile?.name).isEqualTo("exports")
        assertThat(rendered.file.name).isEqualTo("Priya-Deshmukh_Resume.pdf")
        assertThat(rendered.pageCount).isEqualTo(1)
    }
}
