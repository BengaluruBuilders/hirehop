package com.hirehop.core.domain

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.entry
import com.hirehop.core.domain.offline.profileOf
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus
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
    fun addsOnlyKeywordsTheStatementMentionsUsingDisplayNames() = runTest {
        useCase(requirement("sql", "docker", "kubernetes"), "Used Docker for a college project")

        assertThat(checkNotNull(repository.current()).skills).containsExactly("Kotlin", "SQL", "Docker").inOrder()
    }

    @Test
    fun skillAlreadyPresentIsNotDuplicatedIgnoringCase() = runTest {
        useCase(requirement("sql"), "Wrote sql queries for a class project")

        assertThat(checkNotNull(repository.current()).skills).containsExactly("Kotlin", "SQL").inOrder()
    }

    @Test
    fun createsUserStatedEntryWithConfirmedBullet() = runTest {
        useCase(requirement("docker"), "  Used Docker for a college project  ")

        val entry = checkNotNull(repository.current()).entries.single()
        assertThat(entry.id).isEqualTo("U-01")
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

        assertThat(checkNotNull(repository.current()).entries.map { it.id }).containsExactly("e1", "U-01").inOrder()
    }

    @Test
    fun theNewEntryTakesTheNextFreeUncategorisedId() = runTest {
        repository.saveProfile(baseProfile.copy(entries = listOf(entry("U-01", EntryCategory.EDUCATION, "B.Tech", "Degree"))))

        useCase(requirement("docker"), "Used Docker")

        assertThat(checkNotNull(repository.current()).entries.map { it.id }).containsExactly("U-01", "U-02").inOrder()
    }

    @Test
    fun blankStatementSavesNothing() = runTest {
        useCase(requirement("docker", "kubernetes"), "   ")
        useCase(requirement("docker", "kubernetes"), "")

        assertThat(repository.saveCount).isEqualTo(0)
        assertThat(checkNotNull(repository.current())).isEqualTo(baseProfile)
    }

    @Test
    fun unrelatedStatementAddsTheBulletButNoSkills() = runTest {
        useCase(requirement("docker", "kubernetes"), "no")

        val profile = checkNotNull(repository.current())
        assertThat(profile.skills).containsExactly("Kotlin", "SQL").inOrder()
        assertThat(profile.entries.single().bullets.single().text).isEqualTo("no")
    }

    @Test
    fun oneStatementDoesNotAddSiblingKeywords() = runTest {
        useCase(requirement("kubernetes", "docker"), "Ran Docker containers locally")

        assertThat(checkNotNull(repository.current()).skills).doesNotContain("Kubernetes")
    }

    @Test
    fun keywordsStatedInIsAliasAwareAndUsesWordBoundaries() {
        assertThat(keywordsStatedIn(requirement("kubernetes"), "I deployed on K8s")).containsExactly("Kubernetes")
        assertThat(keywordsStatedIn(requirement("java"), "I used JavaScript daily")).isEmpty()
        assertThat(keywordsStatedIn(requirement("java", "javascript"), "Java and JavaScript"))
            .containsExactly("Java", "JavaScript").inOrder()
        assertThat(keywordsStatedIn(requirement("c"), "Wrote C++ code")).isEmpty()
    }

    @Test
    fun keywordsStatedInHandlesKeywordsOutsideTheLexiconByStem() {
        assertThat(keywordsStatedIn(requirement("handling", "customer"), "I handled customer calls"))
            .containsExactly("handling", "customer").inOrder()
        assertThat(keywordsStatedIn(requirement("handling"), "I cooked dinner")).isEmpty()
    }

    @Test
    fun keywordsStatedInPreviewsExactlyWhatTheUseCaseAdds() = runTest {
        val requirement = requirement("docker", "kubernetes", "sql")
        val statement = "Used Docker and MySQL"

        useCase(requirement, statement)

        val added = checkNotNull(repository.current()).skills - baseProfile.skills.toSet()
        assertThat(added).containsExactlyElementsIn(keywordsStatedIn(requirement, statement))
    }

    @Test
    fun noProfileMeansNothingIsSaved() = runTest {
        val empty = FakeProfileRepository(null)

        AddUserStatedFactUseCase(empty, SequentialIdGenerator())(requirement("docker"), "Used Docker")

        assertThat(empty.saveCount).isEqualTo(0)
        assertThat(empty.current()).isNull()
    }

    @Test
    fun previewIsExactlyWhatTheUseCaseSaves() = runTest {
        val previewRepository = FakeProfileRepository(baseProfile)
        val preview = AddUserStatedFactUseCase(previewRepository, SequentialIdGenerator("fact"))
            .preview(requirement("docker", "sql"), "  Used Docker daily  ")
        val savedRepository = FakeProfileRepository(baseProfile)

        AddUserStatedFactUseCase(savedRepository, SequentialIdGenerator("fact"))(
            requirement("docker", "sql"),
            "  Used Docker daily  ",
        )

        assertThat(preview).isEqualTo(savedRepository.current())
        assertThat(previewRepository.saveCount).isEqualTo(0)
    }

    @Test
    fun aStatementCanCloseAGapThroughAnImpliedTermThatKeywordsStatedInDoesNotReturn() = runTest {
        val requirement = requirement("sql")
        val statement = "I ran PostgreSQL in production"
        val repository = FakeProfileRepository(profileOf(emptyList()))
        val preview = checkNotNull(
            AddUserStatedFactUseCase(repository, SequentialIdGenerator("fact")).preview(requirement, statement),
        )

        assertThat(keywordsStatedIn(requirement, statement)).isEmpty()

        val gap = OfflineGapMatcher().match(preview, JobDescription("Data Engineer", "Northwind", "raw", listOf(requirement)))

        assertThat(gap.matches.single { it.requirement.id == requirement.id }.status).isNotEqualTo(MatchStatus.GAP)
    }

    @Test
    fun theUserStatedMarkLandsOnEveryRowTheWordsNameAndOnNoOtherRow() = runTest {
        val sql = requirement("sql").copy(id = "req-sql")
        val docker = requirement("docker").copy(id = "req-docker")
        val kubernetes = requirement("kubernetes").copy(id = "req-kubernetes")
        val job = JobDescription("Data Engineer", "Northwind", "raw", listOf(sql, docker, kubernetes))
        val repository = FakeProfileRepository(profileOf(emptyList()))
        val preview = checkNotNull(
            AddUserStatedFactUseCase(repository, SequentialIdGenerator("fact"))
                .preview(sql, "I wrote SQL for Docker pipelines"),
        )

        val gap = OfflineGapMatcher().match(preview, job)

        fun markedBy(id: String) = gap.matches.single { it.requirement.id == id }
        assertThat(markedBy("req-sql").evidenceIds).contains("fact-1")
        assertThat(markedBy("req-docker").evidenceIds).contains("fact-1")
        assertThat(markedBy("req-kubernetes").status).isEqualTo(MatchStatus.GAP)
        assertThat(markedBy("req-kubernetes").evidenceIds).doesNotContain("fact-1")
    }
}
