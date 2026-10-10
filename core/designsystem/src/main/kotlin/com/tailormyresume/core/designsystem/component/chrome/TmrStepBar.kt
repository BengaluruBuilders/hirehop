package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tailormyresume.core.designsystem.theme.TmrColors

class TmrStepPillStyle(val container: Color, val content: Color)

fun tmrStepPillStyle(step: Int, current: Int, colors: TmrColors): TmrStepPillStyle =
    TmrStepPillStyle(Color.Unspecified, Color.Unspecified)

@Composable
fun TmrStepBar(current: Int, modifier: Modifier = Modifier) = Unit
