package com.tailormyresume.core.network

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.QuickAnswer
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.network.dto.AnalysisRequest
import com.tailormyresume.core.network.dto.TailoringStartRequest
import com.tailormyresume.core.network.mapper.toAnswerDto
import com.tailormyresume.core.network.mapper.toDto
import com.tailormyresume.core.network.mapper.toFactsDto
import org.junit.Test

class AnalysisRequestJsonTest {
    private val json = tailormyresumeJson()
    private val requirement = JobRequirement("req-1", "Strong SQL", RequirementType.SKILL, RequirementPriority.MUST_HAVE, listOf("sql"))
    private val job = JobDescription("Analyst", "Northwind", "raw", listOf(requirement), location = "Bengaluru · Hybrid")
    private val match = RequirementMatch(requirement, MatchStatus.MET, listOf("E1-1"), reason = "SQL in your role.")
    private val profile = CandidateProfile("P", "p@example.com", "+91", "H", listOf("SQL"), emptyList(), summary = "A summary.")

    private fun tailoringBody(answer: QuickAnswer?): String = json.encodeToString(
        TailoringStartRequest.serializer(),
        TailoringStartRequest(
            requestId = "r-1",
            applicationId = "app-1",
            job = job.toDto(),
            matches = listOf(match.toDto()),
            profile = profile.toFactsDto(),
            answer = answer?.toAnswerDto(job),
        ),
    )

    @Test
    fun requestOmitsReasonLocationSectionAndNullAnswer() {
        val tailoring = tailoringBody(null)
        val analysis = json.encodeToString(AnalysisRequest.serializer(), AnalysisRequest("jd text", profile.toFactsDto()))

        listOf("reason", "location", "section", "answer", "null").forEach {
            assertThat(tailoring).doesNotContain(it)
            assertThat(analysis).doesNotContain(it)
        }
        assertThat(tailoring).contains(""""requirementId":"req-1"""")
    }

    @Test
    fun answerIsSentWithoutADetailKeyWhenBlank() {
        val body = tailoringBody(QuickAnswer("req-1", "NOT_YET", "  "))

        assertThat(body).contains(""""answer":{"requirementId":"req-1","choice":"NOT_YET"}""")
    }
}
