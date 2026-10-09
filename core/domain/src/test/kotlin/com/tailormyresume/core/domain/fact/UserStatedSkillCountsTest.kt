package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.ProfileFactCounts
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.core.testing.data.sampleProfile
import org.junit.Test

class UserStatedSkillCountsTest {

    private val base = sampleProfile.copy(skills = listOf("SQL", "Excel"), entries = emptyList())

    @Test
    fun userStatedSkillsCountAsUserStatedAndNotAsConfirmed() {
        val profile = base.copy(userStatedSkills = listOf("sql"))

        assertThat(profile.factCounts()).isEqualTo(ProfileFactCounts(total = 2, confirmed = 1, userStated = 1))
    }

    @Test
    fun skillsWithoutProvenanceStayConfirmed() {
        assertThat(base.factCounts()).isEqualTo(ProfileFactCounts(total = 2, confirmed = 2, userStated = 0))
    }
}
