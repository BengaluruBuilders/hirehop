package com.hirehop.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class HhShapes(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val full: Dp,
)

internal object HhShapesDefaults {
    val Default = HhShapes(
        xs = 4.dp,
        sm = 8.dp,
        md = 12.dp,
        lg = 16.dp,
        full = 999.dp,
    )
}

val LocalHhShapes: ProvidableCompositionLocal<HhShapes> =
    staticCompositionLocalOf { HhShapesDefaults.Default }

internal val HhMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(HhShapesDefaults.Default.xs),
    small = RoundedCornerShape(HhShapesDefaults.Default.sm),
    medium = RoundedCornerShape(HhShapesDefaults.Default.md),
    large = RoundedCornerShape(HhShapesDefaults.Default.lg),
    extraLarge = RoundedCornerShape(HhShapesDefaults.Default.full),
)
