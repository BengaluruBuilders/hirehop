package com.tailormyresume.feature.tailor.impl.diff

internal data class DiffSegment(val text: String, val changed: Boolean)

internal data class WordDiffResult(
    val original: List<DiffSegment>,
    val proposed: List<DiffSegment>,
)

internal fun List<DiffSegment>.joinedText(): String = joinToString(separator = " ") { it.text }

internal object WordDiff {

    internal fun comparisonKey(word: String): String {
        val trimmed = word.trim { it in EDGE_PUNCTUATION }
        return trimmed.ifEmpty { word }
    }

    fun diff(original: String, proposed: String): WordDiffResult {
        val originalWords = original.toWords()
        val proposedWords = proposed.toWords()
        val common =
            longestCommonSubsequence(
                aKeys = originalWords.map { comparisonKey(it) },
                bKeys = proposedWords.map { comparisonKey(it) },
                aSize = originalWords.size,
                bSize = proposedWords.size,
            )
        return WordDiffResult(
            original = originalWords.toSegments(common.originalIndexes),
            proposed = proposedWords.toSegments(common.proposedIndexes),
        )
    }

    private fun String.toWords(): List<String> = trim().split(WHITESPACE).filter { it.isNotEmpty() }

    private val EDGE_PUNCTUATION: Set<Char> =
        setOf(
            '.',
            ',',
            ';',
            ':',
            '!',
            '?',
            '(',
            ')',
            '[',
            ']',
            '{',
            '}',
            '"',
            '\u201C',
            '\u201D',
            '\'',
            '\u2018',
            '\u2019',
            '\u2026',
        )

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

    private fun longestCommonSubsequence(
        aKeys: List<String>,
        bKeys: List<String>,
        aSize: Int,
        bSize: Int,
    ): CommonWords {
        val lengths = Array(aSize + 1) { IntArray(bSize + 1) }
        for (i in aSize - 1 downTo 0) {
            for (j in bSize - 1 downTo 0) {
                lengths[i][j] = if (aKeys[i] == bKeys[j]) {
                    lengths[i + 1][j + 1] + 1
                } else {
                    maxOf(lengths[i + 1][j], lengths[i][j + 1])
                }
            }
        }
        return walkCommonWords(aKeys, bKeys, aSize, bSize, lengths)
    }

    private fun walkCommonWords(
        aKeys: List<String>,
        bKeys: List<String>,
        aSize: Int,
        bSize: Int,
        lengths: Array<IntArray>,
    ): CommonWords {
        val originalIndexes = mutableSetOf<Int>()
        val proposedIndexes = mutableSetOf<Int>()
        var i = 0
        var j = 0
        while (i < aSize && j < bSize) {
            when {
                aKeys[i] == bKeys[j] -> {
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
