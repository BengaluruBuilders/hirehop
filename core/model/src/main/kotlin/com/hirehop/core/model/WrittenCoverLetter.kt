package com.hirehop.core.model

import kotlin.time.Instant

data class WrittenParagraph(
    val text: String,
    val isGreeting: Boolean = false,
    val isUserEdited: Boolean = false,
)

data class WrittenCoverLetter(
    val paragraphs: List<WrittenParagraph>,
    val writtenAt: Instant,
    val generationId: String? = null,
) {
    val wordCount: Int get() = paragraphs.sumOf { paragraph -> paragraph.text.split(WHITESPACE).count { it.isNotEmpty() } }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
