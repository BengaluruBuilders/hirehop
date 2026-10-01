package com.hirehop.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import org.junit.Test

class FactIdAllocatorTest {
    private val allocator = FactIdAllocator()

    @Test
    fun everyCategoryHasItsOwnPrefix() {
        assertThat(nextId(EntryCategory.EDUCATION)).isEqualTo("U-01")
        assertThat(nextId(EntryCategory.EXPERIENCE)).isEqualTo("I-01")
        assertThat(nextId(EntryCategory.PROJECT)).isEqualTo("C-01")
        assertThat(nextId(EntryCategory.CERTIFICATION)).isEqualTo("X-01")
        assertThat(nextId(EntryCategory.ACHIEVEMENT)).isEqualTo("P-01")
    }

    @Test
    fun anEmptyProfileStartsAtOne() {
        assertThat(allocator.nextId(EntryCategory.PROJECT, emptyList())).isEqualTo("C-01")
    }

    @Test
    fun theNumberIsZeroPaddedToTwoDigits() {
        val existing = (1..9).map { entry("C-${it.toString().padStart(2, '0')}") }

        assertThat(allocator.nextId(EntryCategory.PROJECT, existing)).isEqualTo("C-10")
    }

    @Test
    fun theFirstFreeNumberInTheRunIsUsed() {
        val existing = listOf(entry("C-01"), entry("C-02"), entry("C-03"))

        assertThat(allocator.nextId(EntryCategory.PROJECT, existing)).isEqualTo("C-04")
    }

    @Test
    fun aGapInTheRunIsFilledBeforeCountingUp() {
        val existing = listOf(entry("C-01"), entry("C-03"))

        assertThat(allocator.nextId(EntryCategory.PROJECT, existing)).isEqualTo("C-02")
    }

    @Test
    fun aTakenNumberIsNeverReused() {
        val existing = listOf(entry("I-01"), entry("I-02"), entry("I-03"))

        val id = allocator.nextId(EntryCategory.EXPERIENCE, existing)

        assertThat(id).isNotIn(existing.map { it.id })
    }

    @Test
    fun anotherCategoriesIdsDoNotBlockTheNumber() {
        val existing = listOf(entry("U-01"), entry("I-01"), entry("C-01"))

        assertThat(allocator.nextId(EntryCategory.PROJECT, existing)).isEqualTo("C-02")
    }

    @Test
    fun eachCategoryCountsItsOwnRun() {
        val existing = listOf(entry("U-01"), entry("U-02"), entry("I-01"))

        assertThat(allocator.nextId(EntryCategory.EDUCATION, existing)).isEqualTo("U-03")
        assertThat(allocator.nextId(EntryCategory.EXPERIENCE, existing)).isEqualTo("I-02")
    }

    @Test
    fun theCanonicalProfileLeavesRoomInEveryRun() {
        val existing = listOf(entry("U-01"), entry("U-02"), entry("I-01"), entry("I-02"))

        assertThat(allocator.nextId(EntryCategory.EDUCATION, existing)).isEqualTo("U-03")
        assertThat(allocator.nextId(EntryCategory.EXPERIENCE, existing)).isEqualTo("I-03")
    }

    private fun nextId(category: EntryCategory) = allocator.nextId(category, emptyList())

    private fun entry(id: String) = ProfileEntry(
        id = id,
        category = EntryCategory.PROJECT,
        title = "Fact $id",
        organization = "",
        startDate = "",
        endDate = "",
        bullets = listOf(EvidenceBullet("$id-b1", "Text for $id.")),
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )
}
