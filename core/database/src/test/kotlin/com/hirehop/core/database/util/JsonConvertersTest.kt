package com.hirehop.core.database.util

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.database.json.EvidenceBulletDto
import com.hirehop.core.database.json.GapAnalysisDto
import com.hirehop.core.database.json.GuardrailViolationDto
import com.hirehop.core.database.json.JobRequirementDto
import com.hirehop.core.database.json.KeywordCoverageDto
import com.hirehop.core.database.json.RequirementMatchDto
import com.hirehop.core.database.json.TailoredBulletDto
import com.hirehop.core.database.json.TailoredResumeDto
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import org.junit.Test

class JsonConvertersTest {

    private val converters = JsonConverters()

    private val requirement = JobRequirementDto(
        id = "r-1",
        text = "Kotlin \"expert\"\nwith unicode é",
        type = RequirementType.SKILL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = listOf("kotlin"),
    )
    private val gapAnalysis = GapAnalysisDto(
        matches = listOf(RequirementMatchDto(requirement, MatchStatus.PARTIAL, listOf("b-1"))),
        keywordCoverage = KeywordCoverageDto(covered = 1, total = 2),
    )
    private val tailoredResume = TailoredResumeDto(
        bullets = listOf(
            TailoredBulletDto(
                id = "t-1",
                entryId = "e-1",
                originalText = "before",
                proposedText = "after",
                sourceIds = listOf("b-1"),
                editTypes = listOf(EditType.REWORD, EditType.MERGE),
                keywordsUsed = listOf("kotlin"),
                violations = listOf(
                    GuardrailViolationDto.MissingSource,
                    GuardrailViolationDto.VerbEscalation("helped", "led"),
                ),
                decision = BulletDecision.PENDING,
            ),
        ),
    )

    @Test
    fun stringListRoundTrips() {
        val skills = listOf("Kotlin", "C++", "\"quoted\"", "")

        assertThat(converters.jsonToStringList(converters.stringListToJson(skills)))
            .isEqualTo(skills)
    }

    @Test
    fun emptyStringListRoundTrips() {
        assertThat(converters.jsonToStringList(converters.stringListToJson(emptyList())))
            .isEmpty()
    }

    @Test
    fun evidenceBulletsRoundTrip() {
        val bullets = listOf(EvidenceBulletDto("b-1", "Line one\nLine two"))

        assertThat(converters.jsonToEvidenceBullets(converters.evidenceBulletsToJson(bullets)))
            .isEqualTo(bullets)
    }

    @Test
    fun jobRequirementsRoundTrip() {
        val requirements = listOf(requirement)

        assertThat(converters.jsonToJobRequirements(converters.jobRequirementsToJson(requirements)))
            .isEqualTo(requirements)
    }

    @Test
    fun gapAnalysisRoundTrips() {
        assertThat(converters.jsonToGapAnalysis(converters.gapAnalysisToJson(gapAnalysis)))
            .isEqualTo(gapAnalysis)
    }

    @Test
    fun tailoredResumeRoundTripsWithViolations() {
        assertThat(converters.jsonToTailoredResume(converters.tailoredResumeToJson(tailoredResume)))
            .isEqualTo(tailoredResume)
    }

    @Test
    fun tailoredResumeRoundTripsEntryIds() {
        val withIds = tailoredResume.copy(entryIds = listOf("e-1", "edu-1"))

        assertThat(converters.jsonToTailoredResume(converters.tailoredResumeToJson(withIds))).isEqualTo(withIds)
    }

    @Test
    fun tailoredResumeStoredBeforeEntryIdsExistedReadsAsAbsent() {
        val legacyJson = converters.tailoredResumeToJson(tailoredResume)?.replace(Regex(",?\\s*\"entryIds\"\\s*:\\s*null"), "")

        assertThat(converters.jsonToTailoredResume(legacyJson)?.entryIds).isNull()
        assertThat(legacyJson).doesNotContain("entryIds")
    }

    @Test
    fun nullableColumnsMapNullToNull() {
        assertThat(converters.gapAnalysisToJson(null)).isNull()
        assertThat(converters.jsonToGapAnalysis(null)).isNull()
        assertThat(converters.tailoredResumeToJson(null)).isNull()
        assertThat(converters.jsonToTailoredResume(null)).isNull()
    }
}
