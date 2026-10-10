package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.TailoredBullet
import com.tailormyresume.core.model.TailoredResume
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class UpdateBulletDecisionUseCaseTest {
    private val repository = FakeApplicationRepository()
    private val clock = FixedClock(Instant.fromEpochSeconds(2_000_000_000))
    private val useCase = UpdateBulletDecisionUseCase(repository, clock)
    private val created = Instant.fromEpochSeconds(1_000)

    private fun bullet(id: String) = TailoredBullet(
        id = id,
        entryId = "e1",
        originalText = "original $id",
        proposedText = "proposed $id",
        sourceIds = listOf("s-$id"),
        editTypes = emptyList(),
        keywordsUsed = emptyList(),
        violations = emptyList(),
        decision = BulletDecision.PENDING,
    )

    private fun application(resume: TailoredResume?) = JobApplication(
        id = "app-1",
        job = JobDescription("t", "c", "raw", emptyList()),
        status = ApplicationStatus.SAVED,
        gapAnalysis = GapAnalysis(emptyList(), KeywordCoverage(0, 0)),
        tailoredResume = resume,
        createdAt = created,
        updatedAt = created,
    )

    @Test
    fun updatesOnlyTheTargetBulletAndTimestamp() = runTest {
        repository.upsertApplication(application(TailoredResume(listOf(bullet("b1"), bullet("b2")))))

        useCase("app-1", "b2", BulletDecision.ACCEPTED)

        val saved = checkNotNull(repository.current("app-1"))
        assertThat(checkNotNull(saved.tailoredResume).bullets.map { it.decision })
            .containsExactly(BulletDecision.PENDING, BulletDecision.ACCEPTED).inOrder()
        assertThat(saved.updatedAt).isEqualTo(Instant.fromEpochSeconds(2_000_000_000))
        assertThat(saved.createdAt).isEqualTo(created)
    }

    @Test
    fun rejectedDecisionIsStoredAndCanBeChangedBack() = runTest {
        repository.upsertApplication(application(TailoredResume(listOf(bullet("b1")))))

        useCase("app-1", "b1", BulletDecision.REJECTED)
        useCase("app-1", "b1", BulletDecision.PENDING)

        val saved = checkNotNull(repository.current("app-1"))
        assertThat(checkNotNull(saved.tailoredResume).bullets.single().decision).isEqualTo(BulletDecision.PENDING)
    }

    @Test
    fun unknownApplicationDoesNothing() = runTest {
        useCase("missing", "b1", BulletDecision.ACCEPTED)

        assertThat(repository.upsertCount).isEqualTo(0)
    }

    @Test
    fun applicationWithoutTailoredResumeIsUnchanged() = runTest {
        repository.upsertApplication(application(null))
        val before = repository.upsertCount

        useCase("app-1", "b1", BulletDecision.ACCEPTED)

        assertThat(repository.upsertCount).isEqualTo(before)
    }

    @Test
    fun unknownBulletLeavesAllDecisionsUntouched() = runTest {
        repository.upsertApplication(application(TailoredResume(listOf(bullet("b1")))))

        useCase("app-1", "nope", BulletDecision.ACCEPTED)

        val saved = checkNotNull(repository.current("app-1"))
        assertThat(checkNotNull(saved.tailoredResume).bullets.single().decision).isEqualTo(BulletDecision.PENDING)
    }
}
