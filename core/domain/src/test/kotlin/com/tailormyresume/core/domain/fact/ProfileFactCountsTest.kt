package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileFactCounts
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import org.junit.Test

class ProfileFactCountsTest {

    private val entries = canonicalCandidateProfile.entries

    @Test
    fun skillsAndEntriesBothCountAsFacts() {
        val counts = canonicalCandidateProfile.factCounts()

        assertThat(counts.total).isEqualTo(canonicalCandidateProfile.skills.size + entries.size)
    }

    @Test
    fun aConfirmedUserStatedEntryCountsAsUserStatedAndNotAsConfirmed() {
        val stated = entries.first { it.source == FactSource.USER_STATED }.copy(isConfirmed = true)
        val profile = canonicalCandidateProfile.copy(skills = emptyList(), entries = listOf(stated))

        assertThat(profile.factCounts()).isEqualTo(ProfileFactCounts(total = 1, confirmed = 0, userStated = 1))
    }

    @Test
    fun anUnconfirmedEntryIsNeitherConfirmedNorUserStated() {
        val unconfirmed = entries.first().copy(isConfirmed = false, source = FactSource.IMPORTED)
        val profile = canonicalCandidateProfile.copy(skills = listOf("SQL"), entries = listOf(unconfirmed))

        assertThat(profile.factCounts()).isEqualTo(ProfileFactCounts(total = 2, confirmed = 1, userStated = 0))
    }
}
