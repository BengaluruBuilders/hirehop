package com.tailormyresume.feature.tailor.impl.diff

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WordDiffTest {

    @Test
    fun identicalText_hasNoChangedSegments() {
        val result = WordDiff.diff("Built an app", "Built an app")

        assertThat(result.original).containsExactly(DiffSegment("Built an app", changed = false))
        assertThat(result.proposed).containsExactly(DiffSegment("Built an app", changed = false))
    }

    @Test
    fun replacedWord_isMarkedOnBothSides() {
        val result = WordDiff.diff("Built an app", "Developed an app")

        assertThat(result.original).containsExactly(
            DiffSegment("Built", changed = true),
            DiffSegment("an app", changed = false),
        ).inOrder()
        assertThat(result.proposed).containsExactly(
            DiffSegment("Developed", changed = true),
            DiffSegment("an app", changed = false),
        ).inOrder()
    }

    @Test
    fun addedWords_areChangedOnlyInProposed() {
        val result = WordDiff.diff("Built an app", "Built an Android app")

        assertThat(result.original.none { it.changed }).isTrue()
        assertThat(result.proposed).containsExactly(
            DiffSegment("Built an", changed = false),
            DiffSegment("Android", changed = true),
            DiffSegment("app", changed = false),
        ).inOrder()
    }

    @Test
    fun removedWords_areChangedOnlyInOriginal() {
        val result = WordDiff.diff("Built a small app", "Built app")

        assertThat(result.original.filter { it.changed }.map { it.text }).containsExactly("a small")
        assertThat(result.proposed.none { it.changed }).isTrue()
    }

    @Test
    fun segments_joinBackToNormalisedText() {
        val result = WordDiff.diff("  Built   an app  ", "Made\tan  app quickly")

        assertThat(result.original.joinedText()).isEqualTo("Built an app")
        assertThat(result.proposed.joinedText()).isEqualTo("Made an app quickly")
    }

    @Test
    fun emptyText_givesNoSegments() {
        val result = WordDiff.diff("", "Built an app")

        assertThat(result.original).isEmpty()
        assertThat(result.proposed).containsExactly(DiffSegment("Built an app", changed = true))
    }

    @Test
    fun reorderedWords_shareTheSameUnchangedWordsOnBothSides() {
        val result = WordDiff.diff("Led the team and shipped the app", "Shipped the app and led the team")

        val unchangedOriginal = result.original.filter { !it.changed }.flatMap { it.text.split(" ") }
        val unchangedProposed = result.proposed.filter { !it.changed }.flatMap { it.text.split(" ") }
        assertThat(unchangedOriginal).isNotEmpty()
        assertThat(unchangedOriginal).isEqualTo(unchangedProposed)
    }
}
