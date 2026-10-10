package com.tailormyresume.core.designsystem.component.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tailormyresume.core.designsystem.theme.TmrColors
import com.tailormyresume.core.designsystem.theme.TmrMotion

enum class TmrProgressState { Done, Active, Pending }

@Immutable
data class TmrProgressRow(val label: String, val state: TmrProgressState, val meta: String? = null)

@Immutable
data class TmrProgressRowColors(val label: Color, val meta: Color)

internal fun tmrProgressRowColors(state: TmrProgressState, colors: TmrColors): TmrProgressRowColors =
    TmrProgressRowColors(Color.Unspecified, Color.Unspecified)

internal fun tmrSpinnerAngle(frameTimeMs: Long, motion: TmrMotion): Float = -1f

@Composable
fun TmrProgressRows(rows: List<TmrProgressRow>, percent: Int, modifier: Modifier = Modifier) {
}
