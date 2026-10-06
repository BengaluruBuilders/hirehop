package com.hirehop.feature.tailor.impl.export.docx

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import org.junit.Test

class DocxDocumentXmlTest {

    private val document = ResumeDocument(
        name = "Priya Deshmukh",
        contactLine = "priya.d@example.com",
        headline = "",
        skills = listOf("SQL"),
        sections = listOf(
            ResumeSection(
                category = EntryCategory.EXPERIENCE,
                heading = "Heading experience",
                entries = listOf(ResumeEntry("Analyst", "Northwind", "2025", listOf("Built reports"))),
            ),
        ),
        skillsHeading = "Heading skills",
    )

    private fun textOf(xml: String): List<String> =
        Regex("<w:t[^>]*>([^<]*)</w:t>").findAll(xml).map { match -> match.groupValues[1] }.toList()

    @Test
    fun theHeadingsComeFromTheDocument() {
        val words = textOf(DocxDocumentXml.build(document))

        assertThat(words).containsAtLeast("Heading experience", "Heading skills")
    }
}
