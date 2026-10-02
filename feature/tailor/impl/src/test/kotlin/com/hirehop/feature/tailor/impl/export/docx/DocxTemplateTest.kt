package com.hirehop.feature.tailor.impl.export.docx

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.tailor.impl.document.ExportTemplate
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import org.junit.Test

class DocxTemplateTest {

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

    private fun xmlFor(template: ExportTemplate): String =
        DocxDocumentXml.build(document.copy(template = template))

    private fun textOf(xml: String): List<String> =
        Regex("<w:t[^>]*>([^<]*)</w:t>").findAll(xml).map { match -> match.groupValues[1] }.toList()

    @Test
    fun everyTemplateWritesTheSameWords() {
        val plain = textOf(xmlFor(ExportTemplate.PLAIN))

        assertThat(textOf(xmlFor(ExportTemplate.COMPACT))).isEqualTo(plain)
        assertThat(textOf(xmlFor(ExportTemplate.SPACIOUS))).isEqualTo(plain)
    }

    @Test
    fun templatesChangeTheLayoutOnly() {
        val plain = xmlFor(ExportTemplate.PLAIN)

        assertThat(xmlFor(ExportTemplate.COMPACT)).isNotEqualTo(plain)
        assertThat(xmlFor(ExportTemplate.SPACIOUS)).isNotEqualTo(plain)
    }

    @Test
    fun theHeadingsComeFromTheDocument() {
        val words = textOf(xmlFor(ExportTemplate.PLAIN))

        assertThat(words).containsAtLeast("Heading experience", "Heading skills")
    }
}
