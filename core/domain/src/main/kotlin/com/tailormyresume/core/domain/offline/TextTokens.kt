package com.tailormyresume.core.domain.offline

internal object TextTokens {
    private val wordPattern = Regex("[\\p{L}\\p{N}]+")
    private val suffixes = listOf("ing", "ed", "es", "s")
    private const val MIN_STEM_LENGTH = 3

    fun words(text: String): List<String> =
        wordPattern.findAll(text).map { it.value.lowercase() }.toList()

    fun stem(word: String): String {
        val lower = word.lowercase()
        val suffix = suffixes.firstOrNull {
            lower.endsWith(it) && lower.length - it.length >= MIN_STEM_LENGTH
        } ?: return lower
        return lower.removeSuffix(suffix)
    }

    fun stems(text: String): Set<String> = words(text).map(::stem).toSet()
}
