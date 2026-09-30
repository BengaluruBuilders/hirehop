package com.hirehop.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class HhShadow(
    val elevation: Dp,
    val offsetY: Dp,
    val blurRadius: Dp,
    val cornerRadius: Dp,
    val ambientColor: Color,
    val spotColor: Color,
)

@Immutable
data class HhElevation(
    val level1: HhShadow,
    val level2: HhShadow,
    val level3: HhShadow,
)

val LocalHhElevation = staticCompositionLocalOf { HhElevationDefaults.Default }

internal object HhElevationDefaults {
    val Default = HhElevation(
        level1 = HhShadow(
            elevation = 0.dp,
            offsetY = 1.dp,
            blurRadius = 1.dp,
            cornerRadius = 8.dp,
            ambientColor = Color(0x0F000000),
            spotColor = Color(0x0F000000),
        ),
        level2 = HhShadow(
            elevation = 0.dp,
            offsetY = 2.dp,
            blurRadius = 8.dp,
            cornerRadius = 12.dp,
            ambientColor = Color(0x14000000),
            spotColor = Color(0x14000000),
        ),
        level3 = HhShadow(
            elevation = 8.dp,
            offsetY = (-2).dp,
            blurRadius = 16.dp,
            cornerRadius = 16.dp,
            ambientColor = Color(0x1F000000),
            spotColor = Color(0x1F000000),
        ),
    )
}
