package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.tailormyresume.core.domain.offline.resourceText
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProposeJobLabelUseCaseTest {
    private val useCase = ProposeJobLabelUseCase(OfflineJobDescriptionAnalyzer())

    @Test
    fun theProposalMatchesWhatTheAnalyzerFinds() = runTest {
        val raw = resourceText("jd_android.txt")
        val job = OfflineJobDescriptionAnalyzer().analyze(raw)

        val proposal = useCase(raw)

        assertThat(proposal.role).isEqualTo(job.title.trim())
        assertThat(proposal.company).isEqualTo(job.company.trim())
    }

    @Test
    fun blankTextProposesNothing() = runTest {
        assertThat(useCase("   ")).isEqualTo(JobLabelProposal(role = "", company = ""))
    }
}
