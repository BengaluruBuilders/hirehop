package com.hirehop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch

@Composable
fun RequirementStatusDisc(
    status: MatchStatus,
    modifier: Modifier = Modifier,
) {
    val kind = MatchStatusKindMapper().kindOf(status)
    HhStatusDisc(kind = kind, modifier = modifier, contentDescription = kind.label())
}

@Composable
fun RequirementStatusDisc(
    match: RequirementMatch,
    modifier: Modifier = Modifier,
) {
    val kind = MatchStatusKindMapper().kindOf(match)
    HhStatusDisc(kind = kind, modifier = modifier, contentDescription = kind.label())
}
