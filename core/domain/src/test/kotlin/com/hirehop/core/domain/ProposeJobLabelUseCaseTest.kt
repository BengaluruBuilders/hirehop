package com.hirehop.core.domain

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.resourceText
import org.junit.Test

class ProposeJobLabelUseCaseTest {
    private val useCase = ProposeJobLabelUseCase(OfflineJobDescriptionAnalyzer())

    @Test
    fun theProposalMatchesWhatTheAnalyzerFinds() {
        val raw = resourceText("jd_android.txt")
        val job = OfflineJobDescriptionAnalyzer().analyze(raw)

        val proposal = useCase(raw)

        assertThat(proposal.role).isEqualTo(job.title.trim())
        assertThat(proposal.company).isEqualTo(job.company.trim())
    }

    @Test
    fun blankTextProposesNothing() {
        assertThat(useCase("   ")).isEqualTo(JobLabelProposal(role = "", company = ""))
    }
}
