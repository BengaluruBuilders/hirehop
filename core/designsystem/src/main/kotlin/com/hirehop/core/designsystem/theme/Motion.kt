package com.hirehop.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class HhMotion(
    val proof: Int,
    val hop: Int,
    val fade: Int,
    val emphasize: Int,
    val standard: Easing,
    val emphasized: Easing,
    val emphasizedDecelerate: Easing,
    val emphasizedAccelerate: Easing,
    val linear: Easing,
)

val LocalHhMotion = staticCompositionLocalOf { HhMotionDefaults.Default }

internal object HhMotionDefaults {
    val Default = HhMotion(
        proof = 900,
        hop = 180,
        fade = 240,
        emphasize = 160,
        standard = CubicBezierEasing(0.2f, 0f, 0f, 1f),
        emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f),
        emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
        emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f),
        linear = LinearEasing,
    )
}
