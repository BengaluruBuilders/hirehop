package com.hirehop.core.ui

import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchStatusKindMapperTest {

    private val mapper = MatchStatusKindMapper()

    @Test
    fun mapsEveryMatchStatus() {
        assertEquals(HhStatusKind.Met, mapper.kindOf(MatchStatus.MET))
        assertEquals(HhStatusKind.Partial, mapper.kindOf(MatchStatus.PARTIAL))
        assertEquals(HhStatusKind.Gap, mapper.kindOf(MatchStatus.GAP))
    }

    @Test
    fun coversEveryCaseOfTheSourceEnum() {
        val matches = MatchStatus.entries.map { matchFor(it) }
        assertEquals(MatchStatus.entries.size, mapper.kindsOf(matches).size)
        assertEquals(
            listOf(HhStatusKind.Met, HhStatusKind.Partial, HhStatusKind.Gap),
            mapper.kindsOf(matches),
        )
    }

    private fun matchFor(status: MatchStatus) = RequirementMatch(
        requirement = JobRequirement(
            id = "R-01",
            text = "SQL",
            type = RequirementType.SKILL,
            priority = RequirementPriority.MUST_HAVE,
            keywords = listOf("SQL"),
        ),
        status = status,
        evidenceIds = listOf("C-01"),
    )
}
