package com.tailormyresume.core.network

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.network.dto.ProfileFactsDto
import com.tailormyresume.core.network.mapper.toFactsDto
import org.junit.Test

class UserStatedSkillsPayloadTest {
    private val json = tailormyresumeJson()

    private fun profile(skills: List<String>, stated: List<String>) =
        CandidateProfile("P", "", "", "", skills, emptyList(), userStatedSkills = stated)

    private fun wire(profile: CandidateProfile) =
        json.encodeToString(ProfileFactsDto.serializer(), profile.toFactsDto())

    @Test
    fun profileWithoutStatedSkillsHasNoKeyAndSameJson() {
        assertThat(wire(profile(listOf("SQL", "Excel"), emptyList())))
            .isEqualTo("""{"skills":["SQL","Excel"],"entries":[]}""")
    }

    @Test
    fun statedSkillsAreSubsetOfSentSkills() {
        val facts = profile(listOf("SQL", "Excel"), listOf("sql", "Rust")).toFactsDto()

        assertThat(facts.userStatedSkills).containsExactly("SQL")
        assertThat(wire(profile(listOf("SQL", "Excel"), listOf("SQL"))))
            .isEqualTo("""{"skills":["SQL","Excel"],"entries":[],"userStatedSkills":["SQL"]}""")
    }

    @Test
    fun statedSkillDroppedByLimitsIsNotSent() {
        val tooLong = "x".repeat(ProfileLimits.MAX_SKILL_LENGTH + 1)
        val many = List(ProfileLimits.MAX_SKILLS) { "S$it" }
        val facts = profile(many + tooLong + "Late", listOf(tooLong, "Late", "S1")).toFactsDto()

        assertThat(facts.userStatedSkills).containsExactly("S1")
    }

    @Test
    fun statedSkillsAreCappedAtTheSkillLimit() {
        val many = List(ProfileLimits.MAX_SKILLS) { "S$it" }

        assertThat(profile(many, many + many).toFactsDto().userStatedSkills).hasSize(ProfileLimits.MAX_SKILLS)
    }
}
