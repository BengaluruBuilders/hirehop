package com.hirehop.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
class HhShapes(
    val sheet: Shape,
    val heroCard: Shape,
    val card: Shape,
    val field: Shape,
    val pill: Shape,
    val tag: Shape,
    val banner: Shape,
    val pillRow: Shape,
    val statusRow: Shape,
    val modalSheet: Shape,
)

internal object HhShapesDefaults {
    val Default = HhShapes(
        sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        heroCard = RoundedCornerShape(22.dp),
        card = RoundedCornerShape(20.dp),
        field = RoundedCornerShape(18.dp),
        pill = CircleShape,
        tag = RoundedCornerShape(8.dp),
        banner = RoundedCornerShape(18.dp),
        pillRow = RoundedCornerShape(24.dp),
        statusRow = RoundedCornerShape(18.dp),
        modalSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    )
}

val LocalHhShapes: ProvidableCompositionLocal<HhShapes> =
    staticCompositionLocalOf { HhShapesDefaults.Default }

internal val HhMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
