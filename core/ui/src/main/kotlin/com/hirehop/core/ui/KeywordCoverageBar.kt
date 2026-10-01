package com.hirehop.core.ui

import com.hirehop.core.model.KeywordCoverage
import kotlin.math.roundToInt

enum class SegmentFill { FILLED, EMPTY }

class KeywordCoverageBar(segmentCount: Int = DEFAULT_SEGMENT_COUNT) {

    val segmentCount: Int = segmentCount.coerceAtLeast(0)

    fun filledSegmentCount(coverage: KeywordCoverage = noCoverage()): Int {
        val total = coverage.total
        if (total <= 0 || segmentCount <= 0) return 0
        val ratio = coverage.covered.toDouble() / total.toDouble()
        return (ratio * segmentCount).roundToInt().coerceIn(0, segmentCount)
    }

    fun segmentFills(coverage: KeywordCoverage = noCoverage()): List<SegmentFill> {
        val filled = filledSegmentCount(coverage)
        return List(segmentCount) { index -> if (index < filled) SegmentFill.FILLED else SegmentFill.EMPTY }
    }

    companion object {
        const val DEFAULT_SEGMENT_COUNT: Int = 7

        fun noCoverage(): KeywordCoverage = KeywordCoverage(covered = 0, total = 0)
    }
}
