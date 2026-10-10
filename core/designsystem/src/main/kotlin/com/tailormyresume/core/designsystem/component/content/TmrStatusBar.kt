package com.tailormyresume.core.designsystem.component.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tailormyresume.core.designsystem.theme.TmrColors

@Immutable
data class TmrStatusBarSegment(val status: TmrApplicationStatus, val count: Int, val color: Color)

internal fun tmrStatusBarSegments(counts: Map<TmrApplicationStatus, Int>, colors: TmrColors): List<TmrStatusBarSegment> =
    emptyList()

@Composable
fun TmrStatusBar(counts: Map<TmrApplicationStatus, Int>, modifier: Modifier = Modifier) {
}
