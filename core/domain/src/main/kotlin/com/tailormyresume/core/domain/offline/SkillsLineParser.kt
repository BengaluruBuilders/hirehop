package com.tailormyresume.core.domain.offline

internal object SkillsLineParser {
    private const val MAX_SKILL_LENGTH = 50
    private val label = Regex("^[A-Za-z][A-Za-z /&+#.\\-]{1,30}:\\s*")
    private val separators = setOf(',', ';', '|', '•', '·')

    fun parse(line: String): List<String> {
        val content = BulletLine.parse(line).text.replaceFirst(label, "")
        return splitTopLevel(content)
            .map { it.trim().trimEnd('.').trim() }
            .filter { it.isNotEmpty() && it.length <= MAX_SKILL_LENGTH }
    }

    private fun splitTopLevel(text: String): List<String> {
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var depth = 0
        for (char in text) {
            when {
                char == '(' -> depth++
                char == ')' && depth > 0 -> depth--
            }
            if (char in separators && depth == 0) {
                parts += current.toString()
                current.clear()
            } else {
                current.append(char)
            }
        }
        parts += current.toString()
        return parts
    }
}
