package com.hirehop.feature.tailor.impl.diff

internal data class DiffSegment(val text: String, val changed: Boolean)

internal data class WordDiffResult(
    val original: List<DiffSegment>,
    val proposed: List<DiffSegment>,
)

internal fun List<DiffSegment>.joinedText(): String = joinToString(separator = " ") { it.text }

internal object WordDiff {

    fun diff(original: String, proposed: String): WordDiffResult {
        val originalWords = original.toWords()
        val proposedWords = proposed.toWords()
        val common = longestCommonSubsequence(originalWords, proposedWords)
        return WordDiffResult(
            original = originalWords.toSegments(common.originalIndexes),
            proposed = proposedWords.toSegments(common.proposedIndexes),
        )
    }

    private fun String.toWords(): List<String> = trim().split(WHITESPACE).filter { it.isNotEmpty() }

    private fun List<String>.toSegments(unchangedIndexes: Set<Int>): List<DiffSegment> {
        val segments = mutableListOf<DiffSegment>()
        forEachIndexed { index, word ->
            val changed = index !in unchangedIndexes
            val last = segments.lastOrNull()
            if (last != null && last.changed == changed) {
                segments[segments.lastIndex] = last.copy(text = "${last.text} $word")
            } else {
                segments += DiffSegment(text = word, changed = changed)
            }
        }
        return segments
    }

    private fun longestCommonSubsequence(a: List<String>, b: List<String>): CommonWords {
        val lengths = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in a.indices.reversed()) {
            for (j in b.indices.reversed()) {
                lengths[i][j] = if (a[i] == b[j]) {
                    lengths[i + 1][j + 1] + 1
                } else {
                    maxOf(lengths[i + 1][j], lengths[i][j + 1])
                }
            }
        }
        return walkCommonWords(a, b, lengths)
    }

    private fun walkCommonWords(a: List<String>, b: List<String>, lengths: Array<IntArray>): CommonWords {
        val originalIndexes = mutableSetOf<Int>()
        val proposedIndexes = mutableSetOf<Int>()
        var i = 0
        var j = 0
        while (i < a.size && j < b.size) {
            when {
                a[i] == b[j] -> {
                    originalIndexes += i
                    proposedIndexes += j
                    i++
                    j++
                }
                lengths[i + 1][j] >= lengths[i][j + 1] -> i++
                else -> j++
            }
        }
        return CommonWords(originalIndexes, proposedIndexes)
    }

    private data class CommonWords(
        val originalIndexes: Set<Int>,
        val proposedIndexes: Set<Int>,
    )

    private val WHITESPACE = Regex("\\s+")
}
