package com.tailormyresume.core.ui

import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch

class MatchStatusKindMapper {

    fun kindOf(status: MatchStatus): TmrStatusKind =
        when (status) {
            MatchStatus.MET -> TmrStatusKind.Met
            MatchStatus.PARTIAL -> TmrStatusKind.Partial
            MatchStatus.GAP -> TmrStatusKind.Gap
        }

    fun kindOf(match: RequirementMatch): TmrStatusKind = kindOf(match.status)

    fun kindsOf(matches: List<RequirementMatch> = emptyList()): List<TmrStatusKind> = matches.map { kindOf(it) }
}
