package com.hirehop.feature.tailor.impl.coverletter

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.coverletter.CoverLetterDraft
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import org.junit.Test

class CoverLetterCitedFactsTest {

    private val analysis = JobAnalysisResult(
        job = canonicalApplication.job,
        gap = GapAnalysis(emptyList(), KeywordCoverage(0, 0)),
    )
    private val confirmedFactId = confirmedFactsOf(canonicalCandidateProfile).first().factId

    private fun stageOf(citedFactIds: List<String>?) = coverLetterStateFor(
        CoverLetterInputs(
            profile = canonicalCandidateProfile,
            analysis = analysis,
            draft = CoverLetterDraft(
                greeting = "Dear Hiring Manager,",
                openingParagraph = "I am applying for the role.",
                evidenceParagraph = "I worked on reporting for a retail team.",
                closingParagraph = "Thank you for reading.",
                generationId = "g-letter",
                citedFactIds = citedFactIds,
            ),
        ),
    ).stage

    @Test
    fun serverLetterCitingConfirmedFacts_isReadyWithoutAVerbatimQuote() {
        assertThat(stageOf(listOf(confirmedFactId))).isEqualTo(CoverLetterStage.READY)
    }

    @Test
    fun serverLetterCitingAConfirmedEntryOrSkill_isReady() {
        val entryId = canonicalCandidateProfile.entries.first { it.isConfirmed }.id
        val skill = "skill:${canonicalCandidateProfile.skills.first()}"
        assertThat(stageOf(listOf(entryId, skill))).isEqualTo(CoverLetterStage.READY)
    }

    @Test
    fun serverLetterCitingAnUnknownFact_isNotReady() {
        assertThat(stageOf(listOf(confirmedFactId, "not-a-fact"))).isEqualTo(CoverLetterStage.NO_MATCHING_EVIDENCE)
    }

    @Test
    fun serverLetterCitingNothing_isNotReady() {
        assertThat(stageOf(emptyList())).isEqualTo(CoverLetterStage.NO_MATCHING_EVIDENCE)
    }

    @Test
    fun offlineLetterWithoutCitationsAndWithoutAQuote_keepsTheVerbatimRule() {
        assertThat(stageOf(null)).isEqualTo(CoverLetterStage.NO_MATCHING_EVIDENCE)
    }
}
