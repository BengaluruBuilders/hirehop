package com.hirehop.core.domain

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.offline.OfflineFabricationGuard
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.OfflineResumeTailor
import com.hirehop.core.domain.offline.jobDescriptionResources
import com.hirehop.core.domain.offline.resourceText
import com.hirehop.core.domain.offline.sampleProfile
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
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

    private fun useCaseReturning(vararg bullets: TailoredBullet): TailorResumeUseCase {
        val tailor = object : ResumeTailor {
            override fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis) =
                TailoredResume(bullets.toList())
        }
        return TailorResumeUseCase(tailor, OfflineFabricationGuard())
    }

    @Test
    fun cleanBulletKeepsProposedTextAndEditTypes() {
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", "Wrote unit tests with JUnit to improve reliability", "Wrote unit tests with JUnit to improve reliability"),
        )(sampleProfile, emptyJob, emptyGap)

        assertThat(result.bullets.single().violations).isEmpty()
        assertThat(result.bullets.single().editTypes).containsExactly(EditType.REWORD)
    }

    @Test
    fun bulletWithFabricatedNumberFallsBackToOriginalButKeepsViolations() {
        val original = "Wrote unit tests with JUnit to improve reliability"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", original, "Wrote 200 unit tests with JUnit to improve reliability"),
        )(sampleProfile, emptyJob, emptyGap)

        val fallback = result.bullets.single()
        assertThat(fallback.proposedText).isEqualTo(original)
        assertThat(fallback.editTypes).isEmpty()
        assertThat(fallback.violations).containsExactly(GuardrailViolation.UnsupportedNumber("200"))
    }

    @Test
    fun bulletWithEscalatedVerbFallsBackToOriginal() {
        val original = "Assisted senior developers with code reviews on Git"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b3", original, "Led senior developers with code reviews on Git"),
        )(sampleProfile, emptyJob, emptyGap)

        assertThat(result.bullets.single().proposedText).isEqualTo(original)
        assertThat(result.bullets.single().violations)
            .contains(GuardrailViolation.VerbEscalation(from = "assisted", to = "led"))
    }

    @Test
    fun bulletWithUnknownSourceIdFallsBackWithMissingSource() {
        val result = useCaseReturning(bullet("t1", "does-not-exist", "Original", "Changed text"))(
            sampleProfile,
            emptyJob,
            emptyGap,
        )

        assertThat(result.bullets.single().proposedText).isEqualTo("Original")
        assertThat(result.bullets.single().violations).containsExactly(GuardrailViolation.MissingSource)
    }

    @Test
    fun bulletCitingAnUnconfirmedEntryGetsMissingSourceAndTheOriginalText() {
        val result = useCaseReturning(
            bullet("t1", "unconfirmed-1-b1", "Deployed containers with Kubernetes on AWS", "Deployed containers with Kubernetes on AWS"),
        )(sampleProfile, emptyJob, emptyGap)

        val fallback = result.bullets.single()
        assertThat(fallback.violations).containsExactly(GuardrailViolation.MissingSource)
        assertThat(fallback.proposedText).isEqualTo("Deployed containers with Kubernetes on AWS")
        assertThat(fallback.editTypes).isEmpty()
    }

    @Test
    fun fallbackUsesTheProfileSourceTextNotTheTailorsOriginalText() {
        val profileText = "Wrote unit tests with JUnit to improve reliability"
        val lying = bullet("t1", "exp-1-b2", "Led a team of 5", "Led a team of 50 to write tests")
            .copy(keywordsUsed = listOf("junit"))

        val fallback = useCaseReturning(lying)(sampleProfile, emptyJob, emptyGap).bullets.single()

        assertThat(fallback.originalText).isEqualTo(profileText)
        assertThat(fallback.proposedText).isEqualTo(profileText)
        assertThat(fallback.editTypes).isEmpty()
        assertThat(fallback.keywordsUsed).isEmpty()
        assertThat(fallback.violations).isNotEmpty()
    }

    @Test
    fun cleanBulletTakesItsOriginalTextFromTheProfile() {
        val profileText = "Wrote unit tests with JUnit to improve reliability"
        val clean = bullet("t1", "exp-1-b2", "tailor supplied text", profileText)

        val result = useCaseReturning(clean)(sampleProfile, emptyJob, emptyGap).bullets.single()

        assertThat(result.originalText).isEqualTo(profileText)
        assertThat(result.violations).isEmpty()
    }

    @Test
    fun onlyTheViolatingBulletFallsBack() {
        val cleanText = "Wrote unit tests with JUnit to improve reliability"
        val result = useCaseReturning(
            bullet("t1", "exp-1-b2", cleanText, cleanText),
            bullet("t2", "exp-1-b3", "Assisted senior developers with code reviews on Git", "Managed senior developers with code reviews on Git"),
        )(sampleProfile, emptyJob, emptyGap)

        assertThat(result.bullets.map { it.violations.isEmpty() }).containsExactly(true, false).inOrder()
        assertThat(result.bullets[0].editTypes).containsExactly(EditType.REWORD)
        assertThat(result.bullets[1].editTypes).isEmpty()
    }

    @Test
    fun guardRunsOnEveryBulletWithItsOwnSources() {
        val calls = mutableListOf<Pair<String, List<EvidenceBullet>>>()
        val guard = object : FabricationGuard {
            override fun check(proposedText: String, sources: List<EvidenceBullet>, profile: CandidateProfile) =
                emptyList<GuardrailViolation>().also { calls += proposedText to sources }
        }
        val tailor = object : ResumeTailor {
            override fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis) = TailoredResume(
                listOf(
                    bullet("t1", "exp-1-b2", "a", "a1"),
                    bullet("t2", "proj-1-b2", "b", "b1"),
                ),
            )
        }

        TailorResumeUseCase(tailor, guard)(sampleProfile, emptyJob, emptyGap)

        assertThat(calls.map { it.first }).containsExactly("a1", "b1").inOrder()
        assertThat(calls.map { it.second.single().id }).containsExactly("exp-1-b2", "proj-1-b2").inOrder()
    }

    @Test
    fun realTailorOutputPassesTheRealGuardForEveryBulletAcrossFiveJobs() {
        val analyzer = OfflineJobDescriptionAnalyzer()
        val matcher = OfflineGapMatcher()
        val useCase = TailorResumeUseCase(OfflineResumeTailor(), OfflineFabricationGuard())
        jobDescriptionResources.forEach { resource ->
            val job = analyzer.analyze(resourceText(resource))
            val resume = useCase(sampleProfile, job, matcher.match(sampleProfile, job))
            assertThat(resume.bullets).isNotEmpty()
            resume.bullets.forEach { assertThat(it.violations).isEmpty() }
        }
    }
}
