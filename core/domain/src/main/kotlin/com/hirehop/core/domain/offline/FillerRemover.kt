package com.hirehop.core.domain.offline

internal object FillerRemover {
    private const val MIN_WORDS_AFTER_REMOVAL = 3
    private val filler = Regex(
        "\\b(?:responsible for|worked on the task of|in order(?= to\\b)|various|a number of)\\b\\s*",
        RegexOption.IGNORE_CASE,
    )

    fun shorten(text: String): String {
        val stripped = text.replace(filler, "")
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s+([,.;])"), "$1")
            .trim()
            .trimStart(',', ';', ' ')
        if (stripped == text.trim() || stripped.split(" ").size < MIN_WORDS_AFTER_REMOVAL) return text
        return stripped.replaceFirstChar { it.uppercase() }
    }
}
