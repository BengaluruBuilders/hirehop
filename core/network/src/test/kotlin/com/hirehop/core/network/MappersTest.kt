package com.hirehop.core.network

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ContentReport
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.ReportedItemKind
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import com.hirehop.core.network.dto.BulletVerification
import com.hirehop.core.network.dto.MatchDto
import com.hirehop.core.network.dto.TailoredBulletDto
import com.hirehop.core.network.mapper.toDto
import com.hirehop.core.network.mapper.toFactsDto
import com.hirehop.core.network.mapper.toJobDescription
import com.hirehop.core.network.mapper.toRequest
import com.hirehop.core.network.mapper.toRequirementMatches
import com.hirehop.core.network.mapper.toTailoredBullet
import org.junit.Test
import kotlin.time.Instant

class MappersTest {
    private val requirement = JobRequirement("req-1", "SQL", RequirementType.SKILL, RequirementPriority.MUST_HAVE, listOf("sql"))
    private val job = JobDescription("Analyst", "Northwind", "raw jd", listOf(requirement))

    private fun entry(id: String, confirmed: Boolean) = ProfileEntry(
        id, EntryCategory.EXPERIENCE, "Title", "Org", "Jul 2024", "Present",
        listOf(EvidenceBullet("$id-1", "Did a thing")), FactSource.IMPORTED, confirmed,
    )

    @Test
    fun factsSendOnlyConfirmedEntriesAndNoContactDetails() {
        val profile = CandidateProfile("Priya", "p@example.com", "+91", "Headline", listOf("SQL"), listOf(entry("E1", true), entry("E2", false)))
        val facts = profile.toFactsDto()
        assertThat(facts.skills).containsExactly("SQL")
        assertThat(facts.entries.map { it.id }).containsExactly("E1")
        assertThat(facts.entries.single().bullets.single().id).isEqualTo("E1-1")
        assertThat(facts.toString()).doesNotContain("p@example.com")
    }

    @Test
    fun jobSurvivesTheTripAndKeepsLocalRawText() {
        assertThat(job.toDto().toJobDescription("raw jd")).isEqualTo(job)
    }

    @Test
    fun matchesRoundTripAndDropUnknownRequirements() {
        val match = RequirementMatch(requirement, MatchStatus.MET, listOf("E1-1"))
        val dtos = listOf(match.toDto(), MatchDto("req-9", MatchStatus.GAP, emptyList()))
        assertThat(dtos.toRequirementMatches(job)).containsExactly(match)
    }

    @Test
    fun tailoredBulletStartsPendingWithLocalOriginal() {
        val dto = TailoredBulletDto("t-1", "E1", listOf("E1-1"), "New", listOf(EditType.REWORD), listOf("sql"), BulletVerification.PASSED)
        val bullet = dto.toTailoredBullet("Old")
        assertThat(bullet.originalText).isEqualTo("Old")
        assertThat(bullet.proposedText).isEqualTo("New")
        assertThat(bullet.decision).isEqualTo(BulletDecision.PENDING)
        assertThat(bullet.violations).isEmpty()
    }

    @Test
    fun contentReportCarriesGenerationIdAndText() {
        val report = ContentReport("app-1", ReportedItemKind.RESUME_BULLET, "t-1", Instant.fromEpochSeconds(0))
        val request = report.toRequest("gen-1", "text")
        assertThat(request.generationId).isEqualTo("gen-1")
        assertThat(request.itemText).isEqualTo("text")
        assertThat(request.itemKind).isEqualTo(ReportedItemKind.RESUME_BULLET)
    }
}
