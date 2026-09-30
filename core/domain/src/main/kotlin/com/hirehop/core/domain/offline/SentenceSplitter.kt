package com.hirehop.core.domain.offline

internal object SentenceSplitter {
    private const val MIN_LENGTH = 3
    private val boundary = Regex("(?<=[.!?;])\\s+(?=[A-Z0-9])")
    private val abbreviations = listOf("e.g.", "i.e.", "vs.")

    fun split(text: String): List<String> =
        text.split(boundary)
            .fold(mutableListOf<String>()) { parts, piece ->
                val previous = parts.lastOrNull()
                if (previous != null && endsWithAbbreviation(previous)) {
                    parts[parts.lastIndex] = "$previous $piece"
                } else {
                    parts += piece
                }
                parts
            }
            .map { it.trim().trimEnd(';', ',').trim() }
            .filter { it.length >= MIN_LENGTH }

    private fun endsWithAbbreviation(text: String): Boolean {
        val lower = text.lowercase()
        return abbreviations.any { lower.endsWith(it) }
    }
}
