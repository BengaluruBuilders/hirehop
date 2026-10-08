package com.tailormyresume.feature.tailor.impl.export.docx

import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object DocxPackage {

    const val CONTENT_TYPES_PART = "[Content_Types].xml"
    const val ROOT_RELATIONSHIPS_PART = "_rels/.rels"
    const val DOCUMENT_PART = "word/document.xml"
    const val DOCUMENT_RELATIONSHIPS_PART = "word/_rels/document.xml.rels"
    const val CORE_PROPERTIES_PART = "docProps/core.xml"
    const val APP_PROPERTIES_PART = "docProps/app.xml"

    private val requiredParts: List<String> = listOf(
        CONTENT_TYPES_PART,
        ROOT_RELATIONSHIPS_PART,
        DOCUMENT_PART,
        DOCUMENT_RELATIONSHIPS_PART,
        CORE_PROPERTIES_PART,
        APP_PROPERTIES_PART,
    )

    fun write(document: ResumeDocument, out: OutputStream) {
        val parts = mapOf(
            CONTENT_TYPES_PART to contentTypes(),
            ROOT_RELATIONSHIPS_PART to rootRelationships(),
            DOCUMENT_PART to DocxDocumentXml.build(document),
            DOCUMENT_RELATIONSHIPS_PART to documentRelationships(),
            CORE_PROPERTIES_PART to coreProperties(document),
            APP_PROPERTIES_PART to appProperties(),
        )
        ZipOutputStream(out).use { zip ->
            zip.setLevel(Deflater.DEFAULT_COMPRESSION)
            requiredParts.forEach { name -> parts.getValue(name).writeAsPart(zip, name) }
        }
    }

    fun contentTypes(): String = buildString {
        append(DocxXml.DECLARATION)
        append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
        append("<Default Extension=\"rels\" ContentType=\"").append(DocxXml.RELATIONSHIPS_CONTENT_TYPE).append("\"/>")
        append("<Default Extension=\"xml\" ContentType=\"").append(DocxXml.XML_CONTENT_TYPE).append("\"/>")
        append("<Override PartName=\"/word/document.xml\" ContentType=\"").append(DocxXml.MAIN_DOCUMENT_CONTENT_TYPE).append("\"/>")
        append("<Override PartName=\"/docProps/core.xml\" ContentType=\"").append(DocxXml.CORE_PROPERTIES_CONTENT_TYPE).append("\"/>")
        append("<Override PartName=\"/docProps/app.xml\" ContentType=\"").append(DocxXml.EXTENDED_PROPERTIES_CONTENT_TYPE).append("\"/>")
        append("</Types>")
    }

    fun rootRelationships(): String = buildString {
        append(DocxXml.DECLARATION)
        append("<Relationships xmlns=\"").append(DocxXml.PACKAGE_RELATIONSHIPS_NAMESPACE).append("\">")
        append(relationship("rId1", DocxXml.OFFICE_DOCUMENT_RELATIONSHIP, "word/document.xml"))
        append(relationship("rId2", DocxXml.CORE_PROPERTIES_RELATIONSHIP, "docProps/core.xml"))
        append(relationship("rId3", DocxXml.EXTENDED_PROPERTIES_RELATIONSHIP, "docProps/app.xml"))
        append("</Relationships>")
    }

    fun documentRelationships(): String =
        DocxXml.DECLARATION +
            "<Relationships xmlns=\"" + DocxXml.PACKAGE_RELATIONSHIPS_NAMESPACE + "\"></Relationships>"

    fun coreProperties(document: ResumeDocument): String = buildString {
        append(DocxXml.DECLARATION)
        append("<cp:coreProperties xmlns:cp=\"").append(DocxXml.CORE_PROPERTIES_NAMESPACE).append("\"")
        append(" xmlns:dc=\"").append(DocxXml.DUBLIN_CORE_NAMESPACE).append("\">")
        append("<dc:title>").append(DocxXml.escape(document.name)).append("</dc:title>")
        append("<dc:creator>").append(DocxXml.escape(document.name)).append("</dc:creator>")
        append("</cp:coreProperties>")
    }

    fun appProperties(): String = buildString {
        append(DocxXml.DECLARATION)
        append("<Properties xmlns=\"").append(EXTENDED_PROPERTIES_NAMESPACE).append("\"")
        append(" xmlns:vt=\"").append(DOCUMENT_PROPERTIES_TYPES_NAMESPACE).append("\">")
        append("<Application>").append(APPLICATION).append("</Application>")
        append("<Company></Company>")
        append("</Properties>")
    }

    private fun relationship(id: String, type: String, target: String): String =
        "<Relationship Id=\"" + id + "\" Type=\"" + type + "\" Target=\"" + target + "\"/>"

    private fun String.writeAsPart(zip: ZipOutputStream, name: String) {
        val entry = ZipEntry(name)
        entry.setTime(FIXED_ENTRY_TIME)
        zip.putNextEntry(entry)
        zip.write(toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private const val EXTENDED_PROPERTIES_NAMESPACE =
        "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
    private const val DOCUMENT_PROPERTIES_TYPES_NAMESPACE =
        "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"
    private const val APPLICATION = "TailorMyResume"
    private const val FIXED_ENTRY_TIME = 946684800000L
}
