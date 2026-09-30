package com.hirehop.core.domain.offline

internal data class JdLine(val index: Int, val text: String, val section: JdSection)

internal object JdLineReader {
    private val bulletMarker = Regex("^\\s*(?:[•·●▪◦►➢✓✔*>\\-–—]+|\\d{1,2}[.)])\\s+")
    private val standaloneHeader = Regex("^([A-Za-z][A-Za-z '’/&\\-]{1,45}?)\\s*(:)?$")
    private val inlineHeader = Regex("^([A-Za-z][A-Za-z '’/&\\-]{1,45}?)\\s*:\\s*(\\S.*)$")
    private const val MAX_INLINE_LABEL_WORDS = 5

    fun nonEmptyLines(rawText: String): List<String> =
        rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

    fun stripBullet(line: String): String = line.replaceFirst(bulletMarker, "").trim()

    fun read(lines: List<String>): List<JdLine> {
        var section = JdSection.NONE
        val result = mutableListOf<JdLine>()
        lines.forEachIndexed { index, raw ->
            val header = detectHeader(raw)
            if (header != null) section = header.first
            val content = header?.second ?: stripBullet(raw)
            if (content.isNotEmpty()) result += JdLine(index, content, section)
        }
        return result
    }

    private fun detectHeader(raw: String): Pair<JdSection, String>? {
        if (bulletMarker.containsMatchIn(raw)) return null
        val standalone = standaloneHeader.matchEntire(raw)
        if (standalone != null) return standaloneSection(standalone)?.let { it to "" }
        val inline = inlineHeader.matchEntire(raw) ?: return null
        val label = inline.groupValues[1]
        if (label.trim().split(" ").size > MAX_INLINE_LABEL_WORDS) return null
        return JdSectionHeaders.classifyLoose(label)?.let { it to inline.groupValues[2] }
    }

    private fun standaloneSection(match: MatchResult): JdSection? {
        val label = match.groupValues[1]
        val hasColon = match.groupValues[2].isNotEmpty()
        return if (hasColon) JdSectionHeaders.classifyLoose(label) else JdSectionHeaders.classifyExact(label)
    }
}
