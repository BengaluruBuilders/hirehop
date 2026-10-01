package com.hirehop.core.ui

import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch

class MatchStatusKindMapper {

    fun kindOf(status: MatchStatus): HhStatusKind =
        when (status) {
            MatchStatus.MET -> HhStatusKind.Met
            MatchStatus.PARTIAL -> HhStatusKind.Partial
            MatchStatus.GAP -> HhStatusKind.Gap
        }

    fun kindOf(match: RequirementMatch): HhStatusKind = kindOf(match.status)

    fun kindsOf(matches: List<RequirementMatch> = emptyList()): List<HhStatusKind> = matches.map { kindOf(it) }
}
