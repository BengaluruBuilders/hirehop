package com.tailormyresume.core.ui

import com.tailormyresume.core.model.KeywordCoverage
import org.junit.Assert.assertEquals
import org.junit.Test

class KeywordCoverageBarTest {

    @Test
    fun usesSevenSegmentsByDefault() {
        assertEquals(7, KeywordCoverageBar().segmentCount)
        assertEquals(7, KeywordCoverageBar().segmentFills().size)
    }

    @Test
    fun fillsProportionallyAndRoundsHalfUp() {
        val bar = KeywordCoverageBar()
        assertEquals(0, bar.filledSegmentCount(KeywordCoverage(covered = 0, total = 14)))
        assertEquals(4, bar.filledSegmentCount(KeywordCoverage(covered = 7, total = 14)))
        assertEquals(5, bar.filledSegmentCount(KeywordCoverage(covered = 9, total = 14)))
        assertEquals(7, bar.filledSegmentCount(KeywordCoverage(covered = 14, total = 14)))
    }

    @Test
    fun neverDividesByZero() {
        val bar = KeywordCoverageBar()
        assertEquals(0, bar.filledSegmentCount(KeywordCoverage(covered = 0, total = 0)))
        assertEquals(0, bar.filledSegmentCount(KeywordCoverage(covered = 3, total = 0)))
    }

    @Test
    fun clampsOutOfRangeInput() {
        val bar = KeywordCoverageBar()
        assertEquals(7, bar.filledSegmentCount(KeywordCoverage(covered = 20, total = 14)))
        assertEquals(0, bar.filledSegmentCount(KeywordCoverage(covered = -3, total = 14)))
    }

    @Test
    fun handlesANonPositiveSegmentCount() {
        val bar = KeywordCoverageBar(segmentCount = -1)
        assertEquals(0, bar.segmentCount)
        assertEquals(emptyList<SegmentFill>(), bar.segmentFills(KeywordCoverage(covered = 9, total = 14)))
    }

    @Test
    fun listsFilledSegmentsFirst() {
        val bar = KeywordCoverageBar(segmentCount = 4)
        assertEquals(
            listOf(
                SegmentFill.FILLED,
                SegmentFill.EMPTY,
                SegmentFill.EMPTY,
                SegmentFill.EMPTY,
            ),
            bar.segmentFills(KeywordCoverage(covered = 1, total = 4)),
        )
    }
}
