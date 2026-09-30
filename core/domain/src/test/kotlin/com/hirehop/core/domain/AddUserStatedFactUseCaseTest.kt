package com.hirehop.core.domain

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.offline.entry
import com.hirehop.core.domain.offline.profileOf
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddUserStatedFactUseCaseTest {
    private val baseProfile = profileOf(listOf("Kotlin", "SQL"))
    private val repository = FakeProfileRepository(baseProfile)
    private val useCase = AddUserStatedFactUseCase(repository, SequentialIdGenerator("fact"))

    private fun requirement(vararg keywords: String) = JobRequirement(
        id = "req-1",
        text = "Experience with ${keywords.joinToString()}",
        type = RequirementType.TOOL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = keywords.toList(),
    )

    @Test
    fun addsKeywordsToSkillsWithoutCaseInsensitiveDuplicates() = runTest {
        useCase(requirement("sql", "docker", "Docker"), "Used Docker for a college project")

        assertThat(checkNotNull(repository.current()).skills).containsExactly("Kotlin", "SQL", "docker").inOrder()
    }

    @Test
    fun createsUserStatedEntryWithConfirmedBullet() = runTest {
        useCase(requirement("docker"), "  Used Docker for a college project  ")

        val entry = checkNotNull(repository.current()).entries.single()
        assertThat(entry.id).isEqualTo("user-stated")
        assertThat(entry.category).isEqualTo(EntryCategory.ACHIEVEMENT)
        assertThat(entry.title).isEqualTo("Additional experience")
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(entry.isConfirmed).isTrue()
        assertThat(entry.bullets.map { it.text }).containsExactly("Used Docker for a college project")
        assertThat(entry.bullets.single().id).isEqualTo("fact-1")
    }

    @Test
    fun secondFactAppendsToTheSameEntry() = runTest {
        useCase(requirement("docker"), "Used Docker in a hackathon")
        useCase(requirement("aws"), "Deployed a demo to AWS")

        val entries = checkNotNull(repository.current()).entries
        assertThat(entries).hasSize(1)
        assertThat(entries.single().bullets.map { it.text })
            .containsExactly("Used Docker in a hackathon", "Deployed a demo to AWS").inOrder()
        assertThat(entries.single().bullets.map { it.id }).containsExactly("fact-1", "fact-2").inOrder()
    }

    @Test
    fun existingEntriesAreKept() = runTest {
        val existing = entry("e1", EntryCategory.PROJECT, "App", "Built an app")
        repository.saveProfile(baseProfile.copy(entries = listOf(existing)))

        useCase(requirement("docker"), "Used Docker")

        assertThat(checkNotNull(repository.current()).entries.map { it.id }).containsExactly("e1", "user-stated").inOrder()
    }

    @Test
    fun blankStatementAddsSkillsButNoBullet() = runTest {
        useCase(requirement("docker"), "   ")

        val profile = checkNotNull(repository.current())
        assertThat(profile.skills).contains("docker")
        assertThat(profile.entries).isEmpty()
    }

    @Test
    fun noProfileMeansNothingIsSaved() = runTest {
        val empty = FakeProfileRepository(null)

        AddUserStatedFactUseCase(empty, SequentialIdGenerator())(requirement("docker"), "Used Docker")

        assertThat(empty.saveCount).isEqualTo(0)
        assertThat(empty.current()).isNull()
    }
}
