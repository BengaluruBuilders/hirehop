package com.tailormyresume.feature.tailor.impl.diff

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WordDiffPunctuationTest {

    @Test
    fun trailingPeriodOnProposed_marksNothingAsChanged() {
        val result = WordDiff.diff("Built reports", "Built reports.")

        assertThat(result.original).containsExactly(DiffSegment("Built reports", changed = false))
        assertThat(result.proposed).containsExactly(DiffSegment("Built reports.", changed = false))
    }

    @Test
    fun punctuatedTermMatch_isUnchangedOnBothSides() {
        val result = WordDiff.diff("Did cleaning with SQL.", "Cleaned data with SQL for reporting.")

        val changedProposed = result.proposed.filter { it.changed }
        assertThat(changedProposed.joinedText()).doesNotContain("SQL")
        assertThat(result.original.none { it.changed && it.text.contains("SQL.") }).isTrue()
        assertThat(result.proposed.filterNot { it.changed }.flatMap { it.text.split(" ") }).contains("SQL")
        assertThat(result.original.filterNot { it.changed }.flatMap { it.text.split(" ") }).contains("SQL.")
    }

    @Test
    fun addedRealWords_areTheOnlyChangedProposedSegments() {
        val result = WordDiff.diff("Used Excel", "Used Excel and Python")

        assertThat(result.proposed.filter { it.changed }.map { it.text }).containsExactly("and Python")
        assertThat(result.proposed.filterNot { it.changed }.flatMap { it.text.split(" ") }).contains("Excel")
        assertThat(result.original.none { it.changed }).isTrue()
    }

    @Test
    fun newRealWord_isMarkedAsChanged() {
        val result = WordDiff.diff("Built app", "Built fast app")

        assertThat(result.proposed.filter { it.changed }.map { it.text }).containsExactly("fast")
        assertThat(result.original.none { it.changed }).isTrue()
    }

    @Test
    fun tokenWithNoLetterOrDigit_isComparedLiterally() {
        val result = WordDiff.diff("Cut costs -- 20% a year", "Cut costs — 20% a year")

        assertThat(result.original.filter { it.changed }.map { it.text }).containsExactly("--")
        assertThat(result.proposed.filter { it.changed }.map { it.text }).containsExactly("—")
    }
}
