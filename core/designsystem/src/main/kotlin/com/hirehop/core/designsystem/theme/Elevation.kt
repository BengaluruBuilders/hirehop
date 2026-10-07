package com.hirehop.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
class HhElevation(
    val level0: HhShadow,
    val level1: HhShadow,
    val level2: HhShadow,
    val level3: HhShadow,
    val level4: HhShadow,
) {
    val hero: HhShadow get() = level2
    val dock: HhShadow get() = level3
    val modal: HhShadow get() = level4
}

val LocalHhElevation = staticCompositionLocalOf { HhElevationDefaults.Light }

fun Modifier.hhShadow(shadow: HhShadow, shape: Shape): Modifier =
    if (shadow.elevation == 0.dp) {
        this
    } else {
        shadow(
            elevation = shadow.elevation,
            shape = shape,
            ambientColor = shadow.ambientColor,
            spotColor = shadow.spotColor,
        )
    }

internal object HhElevationDefaults {
    private fun step(elevation: Int, offsetY: Int, blur: Int, alpha: Float, ink: Color) = HhShadow(
        elevation = elevation.dp,
        offsetY = offsetY.dp,
        blurRadius = blur.dp,
        cornerRadius = 0.dp,
        ambientColor = ink.copy(alpha = alpha),
        spotColor = ink.copy(alpha = alpha),
    )

    private val LightInk = Color(0xFF000000)
    private val DarkInk = Color(0xFF000000)

    val Light = HhElevation(
        level0 = step(0, 0, 0, 0f, LightInk),
        level1 = step(0, 0, 0, 0f, LightInk),
        level2 = step(3, 3, 12, 0.08f, LightInk),
        level3 = step(4, 4, 16, 0.16f, LightInk),
        level4 = step(8, 8, 24, 0.20f, LightInk),
    )

    val Dark = HhElevation(
        level0 = step(0, 0, 0, 0f, DarkInk),
        level1 = step(0, 0, 0, 0f, DarkInk),
        level2 = step(2, 2, 8, 0.32f, DarkInk),
        level3 = step(4, 4, 16, 0.42f, DarkInk),
        level4 = step(8, 8, 24, 0.48f, DarkInk),
    )
}
