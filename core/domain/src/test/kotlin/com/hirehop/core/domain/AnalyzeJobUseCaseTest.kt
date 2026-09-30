package com.hirehop.core.domain

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.sampleProfile
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import org.junit.Test

class AnalyzeJobUseCaseTest {
    @Test
    fun passesAnalyzerOutputToMatcherAndReturnsBoth() {
        val job = JobDescription("Title", "Company", "raw", emptyList())
        val gap = GapAnalysis(emptyList(), KeywordCoverage(0, 0))
        val received = mutableListOf<Pair<CandidateProfile, JobDescription>>()
        val analyzer = object : JobDescriptionAnalyzer {
            override fun analyze(rawText: String): JobDescription = job
        }
        val matcher = object : GapMatcher {
            override fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis {
                received += profile to job
                return gap
            }
        }
        val useCase = AnalyzeJobUseCase(analyzer, matcher)

        val result = useCase(sampleProfile, "any text")

        assertThat(result).isEqualTo(JobAnalysisResult(job, gap))
        assertThat(received).containsExactly(sampleProfile to job)
    }

    @Test
    fun analysesRawTextEndToEndWithOfflineImplementations() {
        val useCase = AnalyzeJobUseCase(OfflineJobDescriptionAnalyzer(), OfflineGapMatcher())

        val result = useCase(sampleProfile, "Requirements\n- Kotlin\n- Kubernetes")

        assertThat(result.job.requirements).hasSize(2)
        assertThat(result.gap.matches.map { it.status }).containsExactly(MatchStatus.MET, MatchStatus.GAP).inOrder()
        assertThat(result.gap.keywordCoverage).isEqualTo(KeywordCoverage(covered = 1, total = 2))
    }
}
