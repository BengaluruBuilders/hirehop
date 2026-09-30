package com.hirehop.feature.tailor.impl.export.docx

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class DocxPackageTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun write_containsEveryRequiredPartWithTheExpectedName() {
        val names = ZipFile(write("resume.docx")).use { zip -> zip.entryNames() }

        assertThat(names).containsExactlyElementsIn(REQUIRED_PARTS).inOrder()
    }

    @Test
    fun write_producesANonEmptyFile() {
        assertThat(write("resume.docx").length()).isGreaterThan(0L)
    }

    @Test
    fun write_everyRequiredPartIsAReadableWellFormedXmlDocument() {
        val parts = partsOf(write("resume.docx"))

        assertThat(parts.keys).containsExactlyElementsIn(REQUIRED_PARTS)
        parts.forEach { (name, xml) ->
            assertThat(runCatching { parse(xml) }.exceptionOrNull()).isNull()
        }
    }

    @Test
    fun contentTypes_declaresTheMainDocumentPartWithTheWordprocessingContentType() {
        val types = parse(partsOf(write("resume.docx")).getValue(CONTENT_TYPES))

        val mainDocument = types.elements(CONTENT_TYPES_NAMESPACE, TYPE_OVERRIDE)
            .single { it.getAttribute("PartName") == "/word/document.xml" }

        assertThat(mainDocument.getAttribute("ContentType"))
            .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")
    }

    @Test
    fun contentTypes_declaresDefaultsForTheRelationshipAndXmlExtensions() {
        val types = parse(partsOf(write("resume.docx")).getValue(CONTENT_TYPES))

        val defaults = types.elements(CONTENT_TYPES_NAMESPACE, TYPE_DEFAULT)
            .associate { it.getAttribute("Extension") to it.getAttribute("ContentType") }

        assertThat(defaults).containsEntry("rels", "application/vnd.openxmlformats-package.relationships+xml")
        assertThat(defaults).containsEntry("xml", "application/xml")
    }

    @Test
    fun contentTypes_declaresAnOverrideForEveryNonDefaultPart() {
        val types = parse(partsOf(write("resume.docx")).getValue(CONTENT_TYPES))

        val overrides = types.elements(CONTENT_TYPES_NAMESPACE, TYPE_OVERRIDE).map { it.getAttribute("PartName") }

        assertThat(overrides).containsExactly("/word/document.xml", "/docProps/core.xml", "/docProps/app.xml")
    }

    @Test
    fun rootRelationships_pointsAtTheDocumentPartWithTheOfficeDocumentRelationshipType() {
        val relationships = parse(partsOf(write("resume.docx")).getValue(ROOT_RELATIONSHIPS))

        val officeDocument = relationships.elements(RELATIONSHIPS_NAMESPACE, RELATIONSHIP)
            .single { it.getAttribute("Target") == "word/document.xml" }

        assertThat(officeDocument.getAttribute("Type"))
            .isEqualTo("http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument")
    }

    @Test
    fun documentRelationships_partExistsAndDeclaresNoRelationships() {
        val relationships = parse(partsOf(write("resume.docx")).getValue(DOCUMENT_RELATIONSHIPS))

        assertThat(relationships.elements(RELATIONSHIPS_NAMESPACE, RELATIONSHIP)).isEmpty()
    }

    @Test
    fun document_escapesEveryXmlSpecialCharacterInBulletText() {
        val bullet = """Shipped <tool> & "platform" for 'clients'"""

        val body = bodyOf(write("resume.docx", resume(bullets = listOf(bullet))))

        assertThat(body).contains("Shipped &lt;tool&gt; &amp; &quot;platform&quot; for &apos;clients&apos;")
        assertThat(textRuns(body)).contains(bullet)
    }

    @Test
    fun document_rendersTheHeaderSectionsEntryTitlesAndBulletsInOrder() {
        val body = bodyOf(write("resume.docx"))

        assertThat(textRuns(body)).containsAtLeast(
            "Ada Lovelace",
            "ada@example.com",
            "Staff Engineer",
            "Experience",
            "Staff Engineer, Analytical Engines",
            "2020 - 2024",
            "Cut release time in half",
            "Projects",
            "Kotlin, SQL",
        ).inOrder()
    }

    @Test
    fun document_keepsTheSectionOrderOfTheDocument() {
        val body = bodyOf(write("resume.docx"))

        val headings = textRuns(body).filter { it == "Experience" || it == "Projects" }

        assertThat(headings).containsExactly("Experience", "Projects").inOrder()
    }

    @Test
    fun document_joinsTheEntryTitleAndOrganizationTheWayThePdfRendererDoes() {
        val body = bodyOf(write("resume.docx"))

        assertThat(textRuns(body)).contains("Staff Engineer, Analytical Engines")
    }

    @Test
    fun document_survivesALongBulletAnEmptySectionAndAMultiLineBullet() {
        val longBullet = "Delivered a platform migration ".repeat(200)
        val document = resume(
            sections = listOf(
                ResumeSection(EntryCategory.EXPERIENCE, "Experience", listOf(entry(listOf(longBullet, MULTI_LINE_BULLET)))),
                ResumeSection(EntryCategory.PROJECT, "Projects", emptyList()),
            ),
        )

        val body = bodyOf(write("resume.docx", document))

        assertThat(runCatching { parse(body) }.exceptionOrNull()).isNull()
        assertThat(body).contains("<w:br/>")
        assertThat(textRuns(body)).contains(longBullet)
        assertThat(textRuns(body)).contains("First line")
        assertThat(textRuns(body)).contains("Second line")
        assertThat(textRuns(body)).contains("Third line")
    }

    @Test
    fun document_rendersAnEmptyResumeAsAPackageWithEveryPart() {
        val file = write("resume.docx", resume(sections = emptyList(), skills = emptyList(), bullets = emptyList()))

        assertThat(partsOf(file).keys).containsExactlyElementsIn(REQUIRED_PARTS)
        assertThat(runCatching { parse(partsOf(file).getValue(DOCUMENT)) }.exceptionOrNull()).isNull()
    }

    @Test
    fun document_bodyEndsWithTheSectionProperties() {
        assertThat(bodyOf(write("resume.docx"))).endsWith("</w:sectPr></w:body></w:document>")
    }

    @Test
    fun coreProperties_escapesTheCandidateName() {
        val core = partsOf(write("resume.docx", resume(name = "Ada <Lovelace> & Co"))).getValue(CORE_PROPERTIES)

        assertThat(core).contains("Ada &lt;Lovelace&gt; &amp; Co")
        assertThat(runCatching { parse(core) }.exceptionOrNull()).isNull()
    }

    @Test
    fun write_isByteIdenticalForTheSameDocument() {
        val first = write("first.docx").readBytes()
        val second = write("second.docx").readBytes()

        assertThat(second.contentEquals(first)).isTrue()
    }

    @Test
    fun write_pinsEveryZipEntryTimestamp() {
        val first = entryTimesOf(write("first.docx"))
        val second = entryTimesOf(write("second.docx"))

        assertThat(first.distinct()).hasSize(1)
        assertThat(second).isEqualTo(first)
    }

    private fun write(name: String, document: ResumeDocument = resume()): File {
        val file = folder.newFile(name)
        file.outputStream().use { DocxPackage.write(document, it) }
        return file
    }

    private fun ZipFile.entryNames(): List<String> = entries().toList().map { it.name }

    private fun partsOf(file: File): Map<String, String> = ZipFile(file).use { zip ->
        zip.entries().toList().associate { entry ->
            entry.name to zip.getInputStream(entry).readBytes().toString(Charsets.UTF_8)
        }
    }

    private fun entryTimesOf(file: File): List<Long> = ZipFile(file).use { zip -> zip.entries().toList().map { it.time } }

    private fun bodyOf(file: File): String = partsOf(file).getValue(DOCUMENT)

    private fun textRuns(xml: String): List<String> {
        val runs = parse(xml).getElementsByTagNameNS(WORD_NAMESPACE, "t")
        return (0 until runs.length).map { index -> runs.item(index).textContent }
    }

    private fun parse(xml: String): Document = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(InputSource(StringReader(xml)))

    private fun Document.elements(namespace: String, name: String): List<Element> =
        getElementsByTagNameNS(namespace, name).elements()

    private fun NodeList.elements(): List<Element> = (0 until length).map { index -> item(index) as Element }

    private fun resume(
        name: String = "Ada Lovelace",
        sections: List<ResumeSection>? = null,
        skills: List<String> = listOf("Kotlin", "SQL"),
        bullets: List<String> = listOf("Cut release time in half"),
    ): ResumeDocument = ResumeDocument(
        name = name,
        contactLine = "ada@example.com",
        headline = "Staff Engineer",
        skills = skills,
        sections = sections ?: defaultSections(bullets),
    )

    private fun defaultSections(bullets: List<String>): List<ResumeSection> = listOf(
        ResumeSection(EntryCategory.EXPERIENCE, "Experience", listOf(entry(bullets))),
        ResumeSection(EntryCategory.PROJECT, "Projects", listOf(entry(bullets))),
    )

    private fun entry(bullets: List<String> = listOf("Cut release time in half")): ResumeEntry = ResumeEntry(
        title = "Staff Engineer",
        organization = "Analytical Engines",
        dateRange = "2020 - 2024",
        bullets = bullets,
    )

    private companion object {
        const val CONTENT_TYPES = "[Content_Types].xml"
        const val ROOT_RELATIONSHIPS = "_rels/.rels"
        const val DOCUMENT = "word/document.xml"
        const val DOCUMENT_RELATIONSHIPS = "word/_rels/document.xml.rels"
        const val CORE_PROPERTIES = "docProps/core.xml"
        const val APP_PROPERTIES = "docProps/app.xml"
        const val TYPE_DEFAULT = "Default"
        const val TYPE_OVERRIDE = "Override"
        const val RELATIONSHIP = "Relationship"
        const val WORD_NAMESPACE = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
        const val CONTENT_TYPES_NAMESPACE = "http://schemas.openxmlformats.org/package/2006/content-types"
        const val RELATIONSHIPS_NAMESPACE = "http://schemas.openxmlformats.org/package/2006/relationships"
        const val MULTI_LINE_BULLET = "First line\nSecond line\nThird line"
        val REQUIRED_PARTS = listOf(
            CONTENT_TYPES,
            ROOT_RELATIONSHIPS,
            DOCUMENT,
            DOCUMENT_RELATIONSHIPS,
            CORE_PROPERTIES,
            APP_PROPERTIES,
        )
    }
}
