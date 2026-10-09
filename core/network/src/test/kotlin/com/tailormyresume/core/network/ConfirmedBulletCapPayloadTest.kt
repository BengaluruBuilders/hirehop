package com.tailormyresume.core.network

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.confirmedWithinLimits
import com.tailormyresume.core.model.evidenceIds
import com.tailormyresume.core.model.hasTooLongBullet
import com.tailormyresume.core.model.hasTooManyBullets
import org.junit.Test

class ConfirmedBulletCapPayloadTest {
    private val longText = "S" + "a".repeat(448) + "."

    private fun entry(id: String, count: Int, withLong: Boolean = false) = ProfileEntry(
        id, EntryCategory.EXPERIENCE, "Intern", "Acme", "2024", "2025",
        (1..count).map { EvidenceBullet("$id-b$it", if (withLong && it == 2) longText else "Fine $it.") },
        FactSource.IMPORTED, isConfirmed = true,
    )

    private val entries = listOf(
        entry("A", 1),
        entry("B", 15),
        entry("C", 16),
        entry("D", 18),
        entry("F", 30),
        entry("G", 3, withLong = true),
    )

    private val profile = CandidateProfile("P", "", "", "", emptyList(), entries)

    @Test
    fun noConfirmedEntryLosesABulletFromThePayloadWithoutBeingFlagged() {
        val sent = profile.confirmedWithinLimits().entries.associate { it.id to it.bullets.map { b -> b.id } }

        entries.forEach { stored ->
            val missing = stored.bullets.map { it.id } - sent.getValue(stored.id).toSet()
            if (missing.isNotEmpty()) {
                assertThat(stored.hasTooManyBullets || stored.hasTooLongBullet).isTrue()
            }
        }
    }

    @Test
    fun aLeftOutBulletIdIsNeverInTheEvidenceIds() {
        val ids = profile.confirmedWithinLimits().evidenceIds()

        assertThat(ids).contains("D-b15")
        assertThat(ids).doesNotContain("D-b16")
        assertThat(ids).doesNotContain("F-b16")
        assertThat(entries.filter { it.bullets.size > ProfileLimits.MAX_BULLETS_PER_ENTRY }.all { it.hasTooManyBullets })
            .isTrue()
    }
}
