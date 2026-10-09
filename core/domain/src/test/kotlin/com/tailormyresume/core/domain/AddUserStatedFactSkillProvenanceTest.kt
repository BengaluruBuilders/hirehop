package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.profileOf
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddUserStatedFactSkillProvenanceTest {
    private val repository = FakeProfileRepository(profileOf(listOf("Kotlin", "SQL")))
    private val useCase = AddUserStatedFactUseCase(repository, SequentialIdGenerator("fact"))

    @Test
    fun skillsTheUserStatesAreRecordedUserStatedAndExistingSkillsAreNot() = runTest {
        val requirement = JobRequirement(
            id = "req-1",
            text = "Experience with Docker",
            type = RequirementType.TOOL,
            priority = RequirementPriority.MUST_HAVE,
            keywords = listOf("docker", "sql"),
        )

        useCase(requirement, "Used Docker and SQL for a college project")

        assertThat(checkNotNull(repository.current()).userStatedSkills).containsExactly("Docker")
    }
}
