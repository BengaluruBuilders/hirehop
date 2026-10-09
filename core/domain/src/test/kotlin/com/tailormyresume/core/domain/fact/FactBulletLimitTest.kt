package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactUseCase
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.FakeProfileRepository
import com.tailormyresume.core.domain.SequentialIdGenerator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FactBulletLimitTest {
    private val limit = ProfileLimits.MAX_BULLET_LENGTH
    private val seeded = CandidateProfile(
        fullName = "Test Candidate",
        email = "test@example.com",
        phone = "+91 90000 00000",
        headline = "Android engineering student",
        skills = listOf("Kotlin"),
        entries = listOf(
            ProfileEntry(
                id = "project-1",
                category = EntryCategory.PROJECT,
                title = "Kept",
                organization = "",
                startDate = "",
                endDate = "",
                bullets = listOf(EvidenceBullet("project-1-b1", "Existing.")),
                source = FactSource.IMPORTED,
                isConfirmed = true,
            ),
        ),
    )

    private fun draft(detail: String, moreBullets: List<EvidenceBullet> = emptyList()) = FactDraft(
        category = EntryCategory.PROJECT,
        title = "Fact",
        organization = "",
        startDate = "",
        endDate = "",
        detail = detail,
        moreBullets = moreBullets,
    )

    private fun requirement() = JobRequirement(
        id = "req-1",
        text = "Experience with Docker",
        type = RequirementType.TOOL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = listOf("docker"),
    )

    @Test
    fun theValidatorLimitIsTheProfileBulletLimit() {
        assertThat(FactDraftValidator.DETAIL_LIMIT).isEqualTo(limit)
    }

    @Test
    fun theValidatorRejectsADetailOverTheBulletLimit() {
        assertThat(FactDraftValidator.validate(draft("x".repeat(limit + 1))))
            .containsExactly(FactDraftError(FactField.DETAIL, FactDraftErrorReason.TOO_LONG))
        assertThat(FactDraftValidator.validate(draft("x".repeat(limit)))).isEmpty()
    }

    @Test
    fun theValidatorRejectsAnExtraBulletOverTheLimit() {
        val tooLong = EvidenceBullet("b2", "x".repeat(limit + 1))

        assertThat(FactDraftValidator.validate(draft("Short.", listOf(tooLong))))
            .containsExactly(FactDraftError(FactField.DETAIL, FactDraftErrorReason.TOO_LONG))
    }

    @Test
    fun addingFactsRejectsADetailOverTheBulletLimit() = runTest {
        val repository = FakeProfileRepository(seeded)
        val useCase = AddUserStatedFactsUseCase(repository, SequentialIdGenerator("bullet"))

        val outcome = useCase(listOf(draft("x".repeat(limit + 1))))

        assertThat(outcome).isInstanceOf(AddFactsOutcome.Rejected::class.java)
        assertThat(repository.current()).isEqualTo(seeded)
    }

    @Test
    fun attachingABulletEnforcesTheBulletLimit() = runTest {
        val repository = FakeProfileRepository(seeded)
        val useCase = AddUserStatedFactsUseCase(repository, SequentialIdGenerator("bullet"))

        assertThat(useCase.attachBullet("project-1", "x".repeat(limit + 1))).isNull()
        assertThat(useCase.attachBullet("project-1", "x".repeat(limit))).isNotNull()
    }

    @Test
    fun aStatementFromTheGapSheetEnforcesTheBulletLimit() = runTest {
        val repository = FakeProfileRepository(seeded)
        val useCase = AddUserStatedFactUseCase(repository, SequentialIdGenerator("fact"))

        assertThat(useCase.preview(requirement(), "Used Docker " + "x".repeat(limit))).isNull()
        assertThat(useCase.preview(requirement(), "Used Docker " + "x".repeat(limit - 12))).isNotNull()
        assertThat(repository.current()).isEqualTo(seeded)
    }
}
