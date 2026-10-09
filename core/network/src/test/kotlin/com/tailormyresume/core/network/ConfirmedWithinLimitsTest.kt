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
import com.tailormyresume.core.network.mapper.toFactsDto
import org.junit.Test

class ConfirmedWithinLimitsTest {

    private fun entry(bullets: List<EvidenceBullet>) = ProfileEntry(
        "E1", EntryCategory.EXPERIENCE, "Intern", "Acme", "2024", "2025", bullets, FactSource.IMPORTED, isConfirmed = true,
    )

    private fun profile(bullets: List<EvidenceBullet>) =
        CandidateProfile("P", "", "", "", emptyList(), listOf(entry(bullets)))

    private val tooLong = EvidenceBullet("E1-b2", "S" + "a".repeat(448) + ".")

    @Test
    fun overLimitBulletIsLeftOutOfThePayloadNotCut() {
        val stored = listOf(EvidenceBullet("E1-b1", "Short."), tooLong, EvidenceBullet("E1-b3", "Also short."))

        val sent = profile(stored).confirmedWithinLimits().entries.single().bullets

        assertThat(sent.map { it.id }).containsExactly("E1-b1", "E1-b3").inOrder()
        stored.forEach { storedBullet ->
            assertThat(sent.any { it.text != storedBullet.text && storedBullet.text.startsWith(it.text) }).isFalse()
        }
    }

    @Test
    fun noPayloadBulletIsAStrictPrefixOfAStoredBullet() {
        val stored = List(ProfileLimits.MAX_BULLETS_PER_ENTRY + 3) { index ->
            val length = if (index % 2 == 0) 20 + index else ProfileLimits.MAX_BULLET_LENGTH + index
            EvidenceBullet("B$index", "x".repeat(length))
        }

        val payload = profile(stored).toFactsDto().entries.single().bullets

        assertThat(payload).isNotEmpty()
        payload.forEach { sentBullet ->
            assertThat(sentBullet.text).isEqualTo(stored.first { it.id == sentBullet.id }.text)
        }
        assertThat(payload.map { it.id }).containsExactlyElementsIn(
            stored.filter { it.text.length <= ProfileLimits.MAX_BULLET_LENGTH }.map { it.id },
        )
    }

    @Test
    fun evidenceIdsOfTheRequestDoNotIncludeALeftOutBullet() {
        val ids = profile(listOf(EvidenceBullet("E1-b1", "Short."), tooLong)).confirmedWithinLimits().evidenceIds()

        assertThat(ids).containsExactly("E1", "E1-b1")
    }

    @Test
    fun fifteenBulletCapStillHolds() {
        val stored = List(ProfileLimits.MAX_BULLETS_PER_ENTRY + 3) { EvidenceBullet("B$it", "Fine $it.") }

        assertThat(profile(stored).confirmedWithinLimits().entries.single().bullets)
            .hasSize(ProfileLimits.MAX_BULLETS_PER_ENTRY)
    }
}
