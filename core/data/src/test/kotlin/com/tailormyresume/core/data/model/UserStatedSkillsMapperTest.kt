package com.tailormyresume.core.data.model

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.database.model.PopulatedProfile
import com.tailormyresume.core.testing.data.sampleProfile
import org.junit.Test

class UserStatedSkillsMapperTest {

    @Test
    fun userStatedSkillsRoundTripThroughEntities() {
        val profile = sampleProfile.copy(userStatedSkills = listOf("SQL"))

        val restored = PopulatedProfile(profile.asEntity(), profile.asEntryEntities()).asExternalModel()

        assertThat(restored.userStatedSkills).containsExactly("SQL")
    }
}
