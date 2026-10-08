package com.tailormyresume.core.ui

import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchStatusKindMapperTest {

    private val mapper = MatchStatusKindMapper()

    @Test
    fun mapsEveryMatchStatus() {
        assertEquals(TmrStatusKind.Met, mapper.kindOf(MatchStatus.MET))
        assertEquals(TmrStatusKind.Partial, mapper.kindOf(MatchStatus.PARTIAL))
        assertEquals(TmrStatusKind.Gap, mapper.kindOf(MatchStatus.GAP))
    }

    @Test
    fun coversEveryCaseOfTheSourceEnum() {
        val matches = MatchStatus.entries.map { matchFor(it) }
        assertEquals(MatchStatus.entries.size, mapper.kindsOf(matches).size)
        assertEquals(
            listOf(TmrStatusKind.Met, TmrStatusKind.Partial, TmrStatusKind.Gap),
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
