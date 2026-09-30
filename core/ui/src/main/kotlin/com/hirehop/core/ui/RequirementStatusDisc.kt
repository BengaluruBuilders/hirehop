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
    HhStatusDisc(
        kind = MatchStatusKindMapper().kindOf(status),
        modifier = modifier,
    )
}

@Composable
fun RequirementStatusDisc(
    match: RequirementMatch,
    modifier: Modifier = Modifier,
) {
    HhStatusDisc(
        kind = MatchStatusKindMapper().kindOf(match),
        modifier = modifier,
    )
}
