package com.hirehop.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset

@Immutable
class HhProofSpecs(
    val spatial: FiniteAnimationSpec<Float>,
    val spatialFast: FiniteAnimationSpec<Float>,
    val offset: FiniteAnimationSpec<IntOffset>,
    val fade: FiniteAnimationSpec<Float>,
    val staggerMs: Int,
    val staggerMax: Int,
)

@Immutable
class HhHopSpecs(
    val spatial: FiniteAnimationSpec<Float>,
    val scale: FiniteAnimationSpec<Float>,
)

@Immutable
class HhMotion(
    val proofSpecs: HhProofSpecs,
    val hopSpecs: HhHopSpecs,
    val reduced: Boolean,
)

val LocalHhMotion = staticCompositionLocalOf { HhMotionDefaults.Default }

internal object HhMotionDefaults {
    val Default = HhMotion(
        proofSpecs = HhProofSpecs(
            spatial = spring(dampingRatio = 0.9f, stiffness = 600f),
            spatialFast = spring(dampingRatio = 1f, stiffness = 1400f),
            offset = spring(0.9f, 600f, IntOffset.VisibilityThreshold),
            fade = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
            staggerMs = 30,
            staggerMax = 6,
        ),
        hopSpecs = HhHopSpecs(
            spatial = spring(dampingRatio = 0.55f, stiffness = 380f),
            scale = spring(dampingRatio = 0.5f, stiffness = 500f),
        ),
        reduced = false,
    )

    val Reduced = HhMotion(
        proofSpecs = HhProofSpecs(
            spatial = snap(),
            spatialFast = snap(),
            offset = snap(),
            fade = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
            staggerMs = 0,
            staggerMax = 0,
        ),
        hopSpecs = HhHopSpecs(spatial = snap(), scale = snap()),
        reduced = true,
    )
}
