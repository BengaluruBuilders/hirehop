package com.tailormyresume.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.designsystem.component.TmrStatusDisc
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch

@Composable
fun RequirementStatusDisc(
    status: MatchStatus,
    modifier: Modifier = Modifier,
) {
    val kind = MatchStatusKindMapper().kindOf(status)
    TmrStatusDisc(kind = kind, modifier = modifier, contentDescription = kind.label())
}

@Composable
fun RequirementStatusDisc(
    match: RequirementMatch,
    modifier: Modifier = Modifier,
) {
    val kind = MatchStatusKindMapper().kindOf(match)
    TmrStatusDisc(kind = kind, modifier = modifier, contentDescription = kind.label())
}
