package com.tailormyresume.feature.tailor.impl.export.docx

internal data class DocxTextStyle(
    val sizeHalfPoints: Int,
    val bold: Boolean = false,
    val color: String = INK,
) {
    fun runProperties(): String = buildString {
        append(RUN_PROPERTIES_OPEN)
        if (bold) append(BOLD)
        append("<w:color w:val=\"").append(color).append("\"/>")
        append("<w:sz w:val=\"").append(sizeHalfPoints).append("\"/>")
        append("<w:szCs w:val=\"").append(sizeHalfPoints).append("\"/>")
        append(RUN_PROPERTIES_CLOSE)
    }

    companion object {
        const val INK = "111111"
        const val MUTED = "555555"
        const val RULE = "AAAAAA"

        val NAME = DocxTextStyle(sizeHalfPoints = 44, bold = true)
        val HEADLINE = DocxTextStyle(sizeHalfPoints = 22)
        val CONTACT = DocxTextStyle(sizeHalfPoints = 20, color = MUTED)
        val SECTION_HEADING = DocxTextStyle(sizeHalfPoints = 25, bold = true)
        val ENTRY_TITLE = DocxTextStyle(sizeHalfPoints = 22, bold = true)
        val ENTRY_DETAIL = DocxTextStyle(sizeHalfPoints = 20, color = MUTED)
        val BODY = DocxTextStyle(sizeHalfPoints = 21)

        private const val RUN_PROPERTIES_OPEN = "<w:rPr>"
        private const val RUN_PROPERTIES_CLOSE = "</w:rPr>"
        private const val BOLD = "<w:b/>"
    }
}
