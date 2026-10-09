package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.EvidencePicker
import com.tailormyresume.core.domain.prep.PrepQuestionGenerator
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.testing.data.canonicalKotlinRequirement
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TooLongBulletNeverUsedTest {
    private val tooLong = "Built a Kotlin Android app " + "a".repeat(430) + "."
    private val profile = profileOf(
        emptyList(),
        entry("L-1", EntryCategory.EXPERIENCE, "Android developer", tooLong, "Shipped a Kotlin Android app to a small team."),
    )

    private fun analysisCiting(vararg evidenceIds: String) = JobAnalysisResult(
        job = JobDescription("Engineer", "Northwind", "raw", emptyList()),
        gap = GapAnalysis(
            listOf(RequirementMatch(canonicalKotlinRequirement, MatchStatus.MET, evidenceIds.toList())),
            KeywordCoverage(covered = 1, total = 1),
        ),
    )

    @Test
    fun offlineTailorNeverTailorsATooLongBullet() = runTest {
        val job = OfflineJobDescriptionAnalyzer().analyze("Requirements\n- Kotlin")

        val resume = OfflineResumeTailor().tailor(profile, job, OfflineGapMatcher().match(profile, job), "app-1", null)

        assertThat(resume.bullets.flatMap { it.sourceIds }).containsExactly("L-1-b2")
    }

    @Test
    fun coverLetterEvidenceNeverQuotesATooLongBullet() {
        val picked = EvidencePicker.pick(profile, analysisCiting("L-1-b1", "L-1-b2"), maxEvidence = 3)

        assertThat(picked).containsExactly("Shipped a Kotlin Android app to a small team.")
    }

    @Test
    fun prepQuestionsNeverBackAQuestionWithATooLongBullet() {
        val questions = PrepQuestionGenerator.generate(analysisCiting("L-1-b1"), profile)

        assertThat(questions.mapNotNull { it.backingFactId }).doesNotContain("L-1-b1")
        assertThat(questions.joinToString { it.prompt }).doesNotContain(tooLong)
    }
}
