package com.tailormyresume.core.network

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.EditType
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.model.confirmedWithinLimits
import com.tailormyresume.core.model.evidenceIds
import com.tailormyresume.core.network.dto.BulletVerification
import com.tailormyresume.core.network.dto.MatchDto
import com.tailormyresume.core.network.dto.TailoredBulletDto
import com.tailormyresume.core.network.mapper.toDto
import com.tailormyresume.core.network.mapper.toFactsDto
import com.tailormyresume.core.network.mapper.toJobDescription
import com.tailormyresume.core.network.mapper.toRequest
import com.tailormyresume.core.network.mapper.toRequirementMatches
import com.tailormyresume.core.network.mapper.toTailoredBullet
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
    fun factsNeverExceedTheContractLimits() {
        val bullets = List(ProfileLimits.MAX_BULLETS_PER_ENTRY + 3) { EvidenceBullet("B$it", "x".repeat(ProfileLimits.MAX_BULLET_LENGTH + 50)) }
        val big = entry("E1", true).copy(title = "t".repeat(ProfileLimits.MAX_TEXT_LENGTH + 5), bullets = bullets + EvidenceBullet("B-blank", " "))
        val skills = List(ProfileLimits.MAX_SKILLS + 5) { "S$it" } + "s".repeat(ProfileLimits.MAX_SKILL_LENGTH + 1) + " "
        val profile = CandidateProfile("Priya", "p@example.com", "+91", "Headline", skills, listOf(big))

        val facts = profile.toFactsDto()

        assertThat(facts.skills).hasSize(ProfileLimits.MAX_SKILLS)
        assertThat(facts.entries.single().title).hasLength(ProfileLimits.MAX_TEXT_LENGTH)
        assertThat(facts.entries.single().bullets).hasSize(ProfileLimits.MAX_BULLETS_PER_ENTRY)
        assertThat(facts.entries.single().bullets.all { it.text.length == ProfileLimits.MAX_BULLET_LENGTH }).isTrue()
    }

    @Test
    fun evidenceIdsHoldEntryBulletAndSkillIdsOfConfirmedEntriesOnly() {
        val profile = CandidateProfile("P", "", "", "", listOf("SQL"), listOf(entry("E1", true), entry("E2", false)))

        assertThat(profile.confirmedWithinLimits().evidenceIds()).containsExactly("E1", "E1-1", "skill:SQL")
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
        val report = ContentReport("app-1", ReportedItemKind.RESUME_BULLET, "t-1", "text", Instant.fromEpochSeconds(0), "gen-1")
        val request = report.toRequest()
        assertThat(request.generationId).isEqualTo("gen-1")
        assertThat(request.itemText).isEqualTo("text")
        assertThat(request.itemKind).isEqualTo(ReportedItemKind.RESUME_BULLET)
    }
}
