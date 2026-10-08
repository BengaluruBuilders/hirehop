package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Test

class FactDisplayIdsTest {
    private val education = entry("E-01", EntryCategory.EDUCATION, "B.Tech")
    private val work = entry("entry-2", EntryCategory.EXPERIENCE, "Data Associate")
    private val internship = entry("entry-3", EntryCategory.EXPERIENCE, "Data Intern")
    private val secondWork = entry("entry-4", EntryCategory.EXPERIENCE, "Analyst")
    private val entries = listOf(education, work, internship, secondWork)

    @Test
    fun anEntryWithADesignIdKeepsIt() {
        assertThat(FactDisplayIds.of(education, entries)).isEqualTo("E-01")
    }

    @Test
    fun aLegacyEntryGetsThePrefixOfItsCategory() {
        assertThat(FactDisplayIds.of(work, entries)).isEqualTo("W-01")
        assertThat(FactDisplayIds.of(secondWork, entries)).isEqualTo("W-02")
        assertThat(FactDisplayIds.of(internship, entries)).isEqualTo("I-01")
    }

    @Test
    fun aLegacyUserStatedEntryGetsTheUncategorisedPrefix() {
        val stated = entry("user-stated", EntryCategory.ACHIEVEMENT, "Additional experience")

        assertThat(FactDisplayIds.of(stated, entries + stated)).isEqualTo("U-01")
    }

    @Test
    fun aLegacyEntrySkipsANumberThatAnotherEntryHolds() {
        val taken = entry("W-01", EntryCategory.EXPERIENCE, "Engineer")

        assertThat(FactDisplayIds.of(work, entries + taken)).isEqualTo("W-02")
    }

    @Test
    fun aBulletShowsTheFactIdOfItsOwner() {
        assertThat(FactDisplayIds.forBulletId("entry-2-b1", entries)).isEqualTo("W-01")
        assertThat(FactDisplayIds.forBulletId("E-01-b1", entries)).isEqualTo("E-01")
    }

    @Test
    fun anUnknownBulletHasNoDisplayId() {
        assertThat(FactDisplayIds.forBulletId("missing-b1", entries)).isNull()
    }

    @Test
    fun forIdsMapsBulletsAndEntriesAndDropsDuplicates() {
        val ids = FactDisplayIds.forIds(listOf("entry-2-b1", "entry-2", "E-01-b1", "gone"), entries)

        assertThat(ids).containsExactly("W-01", "E-01").inOrder()
    }

    private fun entry(id: String, category: EntryCategory, title: String) = ProfileEntry(
        id = id,
        category = category,
        title = title,
        organization = "",
        startDate = "",
        endDate = "",
        bullets = listOf(EvidenceBullet("$id-b1", "Text for $id.")),
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )
}
