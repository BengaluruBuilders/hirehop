package com.tailormyresume.core.model

val EvidenceBullet.isTooLong: Boolean get() = text.length > ProfileLimits.MAX_BULLET_LENGTH

val ProfileEntry.hasTooLongBullet: Boolean get() = bullets.any { it.isTooLong }

private val sentenceBoundary = Regex("(?<=[.!?।])\\s+(?!\\p{Ll})")

fun fitBulletsToLimit(texts: List<String>): List<String> {
    var roomForExtra = ProfileLimits.MAX_BULLETS_PER_ENTRY - texts.size
    return texts.flatMap { text ->
        val pieces = if (text.length > ProfileLimits.MAX_BULLET_LENGTH) packSentences(text) else listOf(text)
        val extra = pieces.size - 1
        if (extra in 1..roomForExtra) {
            roomForExtra -= extra
            pieces
        } else {
            listOf(text)
        }
    }
}

private fun packSentences(text: String): List<String> {
    val packed = mutableListOf<String>()
    text.split(sentenceBoundary).forEach { sentence ->
        val last = packed.lastOrNull()
        if (last != null && last.length + 1 + sentence.length <= ProfileLimits.MAX_BULLET_LENGTH) {
            packed[packed.lastIndex] = "$last $sentence"
        } else {
            packed += sentence
        }
    }
    return packed
}
