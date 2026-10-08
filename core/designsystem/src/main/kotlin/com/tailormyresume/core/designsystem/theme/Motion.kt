package com.tailormyresume.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

@Immutable
class TmrProofSpecs(
    val spatial: FiniteAnimationSpec<Float>,
    val spatialFast: FiniteAnimationSpec<Float>,
    val offset: FiniteAnimationSpec<IntOffset>,
    val size: FiniteAnimationSpec<IntSize>,
    val fade: FiniteAnimationSpec<Float>,
    val color: FiniteAnimationSpec<Color>,
    val staggerMs: Int,
    val staggerMax: Int,
)

@Immutable
class TmrHopSpecs(
    val spatial: FiniteAnimationSpec<Float>,
    val scale: FiniteAnimationSpec<Float>,
)

@Immutable
class TmrMotion(
    val proofSpecs: TmrProofSpecs,
    val hopSpecs: TmrHopSpecs,
    val reduced: Boolean,
)

val LocalTmrMotion = staticCompositionLocalOf { TmrMotionDefaults.Default }

internal object TmrMotionDefaults {
    val Default = TmrMotion(
        proofSpecs = TmrProofSpecs(
            spatial = spring(dampingRatio = 0.9f, stiffness = 600f),
            spatialFast = spring(dampingRatio = 1f, stiffness = 1400f),
            offset = spring(0.9f, 600f, IntOffset.VisibilityThreshold),
            size = spring(0.9f, 600f, IntSize.VisibilityThreshold),
            fade = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
            color = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
            staggerMs = 30,
            staggerMax = 6,
        ),
        hopSpecs = TmrHopSpecs(
            spatial = spring(dampingRatio = 0.55f, stiffness = 380f),
            scale = spring(dampingRatio = 0.5f, stiffness = 500f),
        ),
        reduced = false,
    )

    val Reduced = TmrMotion(
        proofSpecs = TmrProofSpecs(
            spatial = snap(),
            spatialFast = snap(),
            offset = snap(),
            size = snap(),
            fade = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
            color = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
            staggerMs = 0,
            staggerMax = 0,
        ),
        hopSpecs = TmrHopSpecs(spatial = snap(), scale = snap()),
        reduced = true,
    )
}
