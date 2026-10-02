package com.hirehop.feature.tailor.impl.export.docx

import com.hirehop.feature.tailor.impl.document.ExportTemplate
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import kotlin.math.roundToInt

internal object DocxDocumentXml {

    fun build(document: ResumeDocument): String = buildString {
        append(DocxXml.DECLARATION)
        append("<w:document xmlns:w=\"").append(DocxXml.WORD_NAMESPACE).append("\"><w:body>")
        append(DocxLayout(document.template).body(document))
        append(SECTION_PROPERTIES)
        append("</w:body></w:document>")
    }

    private class DocxLayout(template: ExportTemplate) {
        private val textScale = template.textScale
        private val tight = scaled(TIGHT, template)
        private val bulletGap = scaled(BULLET_GAP, template)
        private val ruleGap = scaled(RULE_GAP, template)
        private val entryGap = scaled(ENTRY_GAP, template)
        private val sectionGap = scaled(SECTION_GAP, template)
        private val name = DocxTextStyle.NAME.scaled(textScale)
        private val contact = DocxTextStyle.CONTACT.scaled(textScale)
        private val headline = DocxTextStyle.HEADLINE.scaled(textScale)
        private val sectionHeading = DocxTextStyle.SECTION_HEADING.scaled(textScale)
        private val entryTitle = DocxTextStyle.ENTRY_TITLE.scaled(textScale)
        private val entryDetail = DocxTextStyle.ENTRY_DETAIL.scaled(textScale)
        private val bodyStyle = DocxTextStyle.BODY.scaled(textScale)

        fun body(document: ResumeDocument): String = buildString {
            document.name.writeParagraph(this, name, after = tight)
            document.contactLine.writeParagraph(this, contact, after = tight)
            document.headline.writeParagraph(this, headline, after = sectionGap)
            document.sections.forEach { section(out = this, section = it) }
            skills(out = this, document = document)
        }

        private fun section(out: StringBuilder, section: ResumeSection) {
            section.heading.writeParagraph(
                out = out,
                style = sectionHeading,
                before = sectionGap,
                after = ruleGap,
                rule = true,
            )
            section.entries.forEach { entry(out = out, entry = it) }
        }

        private fun entry(out: StringBuilder, entry: ResumeEntry) {
            val titleLine = listOf(entry.title, entry.organization)
                .filter { it.isNotEmpty() }
                .joinToString(", ")
            titleLine.writeParagraph(out, entryTitle, after = tight)
            entry.dateRange.writeParagraph(out, entryDetail, after = entryGap)
            entry.bullets.forEach { bullet(out = out, text = it) }
        }

        private fun skills(out: StringBuilder, document: ResumeDocument) {
            if (document.skills.isEmpty()) return
            document.skillsHeading.writeParagraph(
                out = out,
                style = sectionHeading,
                before = sectionGap,
                after = ruleGap,
                rule = true,
            )
            document.skills.joinToString(", ").writeParagraph(out, bodyStyle, after = sectionGap)
        }

        private fun bullet(out: StringBuilder, text: String) {
            if (text.isEmpty()) return
            BULLET_MARKER.writeParagraph(out, bodyStyle, after = tight, hanging = true)
            text.writeParagraph(out, bodyStyle, after = bulletGap, hanging = true)
        }
    }

    private fun String.writeParagraph(
        out: StringBuilder,
        style: DocxTextStyle,
        before: Int = 0,
        after: Int = 0,
        rule: Boolean = false,
        hanging: Boolean = false,
    ) {
        if (isEmpty()) return
        val spacing = before > 0 || after > 0
        out.append("<w:p><w:pPr>")
        if (rule) {
            out.append("<w:pBdr><w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"")
                .append(DocxTextStyle.RULE)
                .append("\"/></w:pBdr>")
        }
        if (spacing) {
            out.append("<w:spacing w:before=\"").append(before).append("\" w:after=\"").append(after).append("\"/>")
        }
        if (hanging) {
            out.append("<w:ind w:left=\"").append(BULLET_INDENT).append("\" w:hanging=\"")
                .append(BULLET_INDENT)
                .append("\"/>")
        }
        out.append(style.runProperties())
        out.append("</w:pPr><w:r>")
        DocxXml.appendRunWithBreaks(out, this)
        out.append("</w:r></w:p>")
    }

    private fun scaled(points: Int, template: ExportTemplate): Int = (points * template.spaceScale).roundToInt()

    private const val BULLET_MARKER = "•"
    private const val BULLET_INDENT = 280
    private const val TIGHT = 40
    private const val BULLET_GAP = 40
    private const val RULE_GAP = 60
    private const val ENTRY_GAP = 120
    private const val SECTION_GAP = 240
    private const val SECTION_PROPERTIES =
        "<w:sectPr><w:pgSz w:w=\"11900\" w:h=\"16840\"/>" +
            "<w:pgMar w:top=\"960\" w:right=\"960\" w:bottom=\"960\" w:left=\"960\" " +
            "w:header=\"480\" w:footer=\"480\" w:gutter=\"0\"/></w:sectPr>"
}
