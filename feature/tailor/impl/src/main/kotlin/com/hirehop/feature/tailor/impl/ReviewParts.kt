package com.hirehop.feature.tailor.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.icon.HhIcons

@Composable
internal fun DecisionChip(state: BulletReviewState, modifier: Modifier = Modifier) {
    StatusPill(
        label = stringResource(state.labelChipRes()),
        icon = state.chipIcon(),
        color = state.chipColor(),
        modifier = modifier,
    )
}

@Composable
internal fun DecideNote(modifier: Modifier = Modifier) {
    NoteLine(
        text = stringResource(R.string.feature_tailor_impl_decide_note),
        icon = HhIcons.Verified,
        modifier = modifier,
    )
}
