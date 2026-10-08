package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.OfflineFabricationGuard
import com.tailormyresume.core.domain.offline.OfflineGapMatcher
import com.tailormyresume.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.tailormyresume.core.domain.offline.OfflineResumeTailor
import com.tailormyresume.core.domain.offline.jobDescriptionResources
import com.tailormyresume.core.domain.offline.resourceText
import com.tailormyresume.core.domain.offline.sampleProfile
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EditType
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.GuardrailViolation
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.TailoredBullet
import com.tailormyresume.core.model.TailoredResume
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TailorResumeUseCaseTest {
    private val emptyJob = JobDescription("", "", "", emptyList())
    private val emptyGap = GapAnalysis(emptyList(), KeywordCoverage(0, 0))

    private fun bullet(
        id: String,
        sourceId: String,
        original: String,
        proposed: String,
        editTypes: List<EditType> = listOf(EditType.REWORD),
    ) = TailoredBullet(
        id = id,
        entryId = "exp-1",
        originalText = original,
        proposedText = proposed,
        sourceIds = listOf(sourceId),
        editTypes = editTypes,
        keywordsUsed = emptyList(),
        violations = emptyList(),
        decision = BulletDecision.PENDING,
    )

    private suspend fun useCaseReturning(vararg bullets: TailoredBullet): TailorResumeUseCase {
        val tailor = object : ResumeTailor {
            override suspend fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis, applicationId: String, section: EntryCategory?) =
                TailoredResume(bullets.toList())
        }
        return TailorResumeUseCase(tailor, OfflineFabricationGuard())
    }

    @Test
    fun cleanBulletKeepsProposedTextAndEditTypes() = runTest {
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", "Wrote unit tests with JUnit to improve reliability", "Wrote unit tests with JUnit to improve reliability"),
        )(sampleProfile, emptyJob, emptyGap, "app-1")

        assertThat(result.bullets.single().violations).isEmpty()
        assertThat(result.bullets.single().editTypes).containsExactly(EditType.REWORD)
    }

    @Test
    fun bulletWithFabricatedNumberFallsBackToOriginalButKeepsViolations() = runTest {
        val original = "Wrote unit tests with JUnit to improve reliability"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", original, "Wrote 200 unit tests with JUnit to improve reliability"),
        )(sampleProfile, emptyJob, emptyGap, "app-1")

        val fallback = result.bullets.single()
        assertThat(fallback.proposedText).isEqualTo(original)
        assertThat(fallback.editTypes).isEmpty()
        assertThat(fallback.violations).containsExactly(GuardrailViolation.UnsupportedNumber("200"))
    }

    @Test
    fun bulletWithEscalatedVerbFallsBackToOriginal() = runTest {
        val original = "Assisted senior developers with code reviews on Git"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b3", original, "Led senior developers with code reviews on Git"),
        )(sampleProfile, emptyJob, emptyGap, "app-1")

        assertThat(result.bullets.single().proposedText).isEqualTo(original)
        assertThat(result.bullets.single().violations)
            .contains(GuardrailViolation.VerbEscalation(from = "assisted", to = "led"))
    }

    @Test
    fun bulletWithUnknownSourceIdIsDropped() = runTest {
        val result = useCaseReturning(bullet("t1", "does-not-exist", "Original", "Changed text"))(
            sampleProfile,
            emptyJob,
            emptyGap,
            "app-1",
        )

        assertThat(result.bullets).isEmpty()
    }

    @Test
    fun bulletCitingAnUnconfirmedEntryIsDropped() = runTest {
        val text = "Deployed containers with Kubernetes on AWS"
        val result = useCaseReturning(bullet("t1", "unconfirmed-1-b1", text, text))(
            sampleProfile,
            emptyJob,
            emptyGap,
            "app-1",
        )

        assertThat(result.bullets).isEmpty()
    }

    @Test
    fun resultRecordsTheIdsOfTheConfirmedEntriesUsed() = runTest {
        val result = useCaseReturning()(sampleProfile, emptyJob, emptyGap, "app-1")

        assertThat(result.entryIds).containsExactlyElementsIn(sampleProfile.entries.filter { it.isConfirmed }.map { it.id })
        assertThat(result.entryIds).doesNotContain("unconfirmed-1")
    }

    @Test
    fun bulletWithNoSourceIdsIsDroppedAndOthersAreKept() = runTest {
        val text = "Wrote unit tests with JUnit to improve reliability"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", text, text).copy(sourceIds = emptyList()),
            bullet("t2", "does-not-exist", "x", "y"),
            bullet("t3", "exp-1-b2", text, text),
        )(sampleProfile, emptyJob, emptyGap, "app-1")

        assertThat(result.bullets.map { it.id }).containsExactly("t3")
    }

    @Test
    fun fallbackUsesTheProfileSourceTextNotTheTailorsOriginalText() = runTest {
        val profileText = "Wrote unit tests with JUnit to improve reliability"
        val lying = bullet("t1", "exp-1-b2", "Led a team of 5", "Led a team of 50 to write tests")
            .copy(keywordsUsed = listOf("junit"))

        val fallback = useCaseReturning(lying)(sampleProfile, emptyJob, emptyGap, "app-1").bullets.single()

        assertThat(fallback.originalText).isEqualTo(profileText)
        assertThat(fallback.proposedText).isEqualTo(profileText)
        assertThat(fallback.editTypes).isEmpty()
        assertThat(fallback.keywordsUsed).isEmpty()
        assertThat(fallback.violations).isNotEmpty()
    }

    @Test
    fun cleanBulletTakesItsOriginalTextFromTheProfile() = runTest {
        val profileText = "Wrote unit tests with JUnit to improve reliability"
        val clean = bullet("t1", "exp-1-b2", "tailor supplied text", profileText)

        val result = useCaseReturning(clean)(sampleProfile, emptyJob, emptyGap, "app-1").bullets.single()

        assertThat(result.originalText).isEqualTo(profileText)
        assertThat(result.violations).isEmpty()
    }

    @Test
    fun onlyTheViolatingBulletFallsBack() = runTest {
        val cleanText = "Wrote unit tests with JUnit to improve reliability"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", cleanText, cleanText),
            bullet("t2", "exp-1-b3", "Assisted senior developers with code reviews on Git", "Managed senior developers with code reviews on Git"),
        )(sampleProfile, emptyJob, emptyGap, "app-1")

        assertThat(result.bullets.map { it.violations.isEmpty() }).containsExactly(true, false).inOrder()
        assertThat(result.bullets[0].editTypes).containsExactly(EditType.REWORD)
        assertThat(result.bullets[1].editTypes).isEmpty()
    }

    @Test
    fun guardRunsOnEveryBulletWithItsOwnSources() = runTest {
        val calls = mutableListOf<Pair<String, List<EvidenceBullet>>>()
        val guard = object : FabricationGuard {
            override fun check(proposedText: String, sources: List<EvidenceBullet>, profile: CandidateProfile) =
                emptyList<GuardrailViolation>().also { calls += proposedText to sources }
        }
        val tailor = object : ResumeTailor {
            override suspend fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis, applicationId: String, section: EntryCategory?) = TailoredResume(
                listOf(
                    bullet("t1", "exp-1-b2", "a", "a1"),
                    bullet("t2", "proj-1-b2", "b", "b1"),
                ),
            )
        }

        TailorResumeUseCase(tailor, guard)(sampleProfile, emptyJob, emptyGap, "app-1")

        assertThat(calls.map { it.first }).containsExactly("a1", "b1").inOrder()
        assertThat(calls.map { it.second.single().id }).containsExactly("exp-1-b2", "proj-1-b2").inOrder()
    }

    @Test
    fun realTailorOutputPassesTheRealGuardForEveryBulletAcrossFiveJobs() = runTest {
        val analyzer = OfflineJobDescriptionAnalyzer()
        val matcher = OfflineGapMatcher()
        val useCase = TailorResumeUseCase(OfflineResumeTailor(), OfflineFabricationGuard())
        jobDescriptionResources.forEach { resource ->
            val job = analyzer.analyze(resourceText(resource))
            val resume = useCase(sampleProfile, job, matcher.match(sampleProfile, job), "app-1")
            assertThat(resume.bullets).isNotEmpty()
            resume.bullets.forEach { assertThat(it.violations).isEmpty() }
        }
    }
}
