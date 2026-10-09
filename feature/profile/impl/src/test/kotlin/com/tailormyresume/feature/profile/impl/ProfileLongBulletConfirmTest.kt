package com.tailormyresume.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Test

class ProfileLongBulletConfirmTest {

    private fun entry(id: String, text: String) = ProfileEntry(
        id, EntryCategory.EXPERIENCE, "Intern", "Acme", "2024", "2025",
        listOf(EvidenceBullet("$id-b1", text)), FactSource.IMPORTED, isConfirmed = false,
    )

    private val profile = CandidateProfile(
        "P",
        "",
        "",
        "",
        emptyList(),
        listOf(entry("LONG", "S" + "a".repeat(448) + "."), entry("OK", "Short.")),
    )

    @Test
    fun confirmEntryLeavesAnEntryWithATooLongBulletOpen() {
        val confirmed = profile.confirmEntry("LONG")

        assertThat(confirmed.entries.first { it.id == "LONG" }.isConfirmed).isFalse()
    }

    @Test
    fun confirmEntryStillConfirmsAnEntryWithinTheLimit() {
        assertThat(profile.confirmEntry("OK").entries.first { it.id == "OK" }.isConfirmed).isTrue()
    }
}
