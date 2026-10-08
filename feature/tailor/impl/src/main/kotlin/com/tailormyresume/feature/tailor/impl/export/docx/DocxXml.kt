package com.tailormyresume.feature.tailor.impl.export.docx

internal object DocxXml {

    const val DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"

    const val WORD_NAMESPACE = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    const val PACKAGE_RELATIONSHIPS_NAMESPACE = "http://schemas.openxmlformats.org/package/2006/relationships"
    const val OFFICE_DOCUMENT_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
    const val EXTENDED_PROPERTIES_RELATIONSHIP = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties"
    const val CORE_PROPERTIES_RELATIONSHIP = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties"

    const val MAIN_DOCUMENT_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"
    const val CORE_PROPERTIES_CONTENT_TYPE = "application/vnd.openxmlformats-package.core-properties+xml"
    const val EXTENDED_PROPERTIES_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.extended-properties+xml"
    const val RELATIONSHIPS_CONTENT_TYPE = "application/vnd.openxmlformats-package.relationships+xml"
    const val XML_CONTENT_TYPE = "application/xml"

    const val CORE_PROPERTIES_NAMESPACE = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
    const val DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/elements/1.1/"

    fun escape(text: String): String = buildString(text.length) {
        text.forEach { character -> appendEscaped(character) }
    }

    fun appendRunWithBreaks(out: StringBuilder, text: String) {
        val normalised = text.replace(WINDOWS_BREAK, LINE_FEED).replace(CARRIAGE_RETURN, LINE_BREAK)
        normalised.split(LINE_FEED).forEachIndexed { index, line ->
            if (index > 0) out.append(LINE_BREAK_ELEMENT)
            out.append(TEXT_OPEN)
            out.append(escape(line))
            out.append(TEXT_CLOSE)
        }
    }

    private fun StringBuilder.appendEscaped(character: Char) {
        when (character) {
            AMPERSAND -> append(AMPERSAND_ESCAPE)
            LESS_THAN -> append(LESS_THAN_ESCAPE)
            GREATER_THAN -> append(GREATER_THAN_ESCAPE)
            DOUBLE_QUOTE -> append(DOUBLE_QUOTE_ESCAPE)
            SINGLE_QUOTE -> append(SINGLE_QUOTE_ESCAPE)
            else -> if (character.isAllowed()) append(character)
        }
    }

    private fun Char.isAllowed(): Boolean =
        this == TAB || this == LINE_BREAK || this == CARRIAGE_RETURN || this >= SPACE

    private const val AMPERSAND = '&'
    private const val LESS_THAN = '<'
    private const val GREATER_THAN = '>'
    private const val DOUBLE_QUOTE = '"'
    private const val SINGLE_QUOTE = '\''
    private const val TAB = '\t'
    private const val LINE_BREAK = '\n'
    private const val LINE_FEED = "\n"
    private const val CARRIAGE_RETURN = '\r'
    private const val SPACE = ' '
    private const val WINDOWS_BREAK = "\r\n"

    private const val AMPERSAND_ESCAPE = "&amp;"
    private const val LESS_THAN_ESCAPE = "&lt;"
    private const val GREATER_THAN_ESCAPE = "&gt;"
    private const val DOUBLE_QUOTE_ESCAPE = "&quot;"
    private const val SINGLE_QUOTE_ESCAPE = "&apos;"
    private const val LINE_BREAK_ELEMENT = "<w:br/>"
    private const val TEXT_OPEN = "<w:t xml:space=\"preserve\">"
    private const val TEXT_CLOSE = "</w:t>"
}
