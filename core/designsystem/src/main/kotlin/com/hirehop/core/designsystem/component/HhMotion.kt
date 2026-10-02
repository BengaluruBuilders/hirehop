package com.hirehop.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhProofSpecs
import com.hirehop.core.designsystem.theme.HhTheme
import kotlinx.coroutines.delay

internal val HhRise = 12.dp

internal val HhSheetRise = 48.dp

internal val HhTabShift = 24.dp

private const val HH_PRESS_SCALE = 0.97f

@Composable
fun HhVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    rise: Boolean = false,
    content: @Composable () -> Unit,
) {
    val proof = HhTheme.motion.proofSpecs
    val shift = with(LocalDensity.current) { HhRise.roundToPx() }
    val enter = if (rise && !HhTheme.motion.reduced) {
        fadeIn(proof.fade) + slideInVertically(proof.offset) { shift }
    } else {
        fadeIn(proof.fade)
    }
    val exit = if (rise && !HhTheme.motion.reduced) {
        fadeOut(proof.fade) + slideOutVertically(proof.offset) { shift }
    } else {
        fadeOut(proof.fade)
    }
    AnimatedVisibility(visible = visible, modifier = modifier, enter = enter, exit = exit) {
        content()
    }
}

@Composable
fun <T> HhContentSwitch(
    targetState: T,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any? = { it },
    content: @Composable (T) -> Unit,
) {
    val fade = HhTheme.motion.proofSpecs.fade
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) using null },
        contentKey = contentKey,
        label = "hhContentSwitch",
    ) { state ->
        content(state)
    }
}

@Composable
fun HhExpandable(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val proof = HhTheme.motion.proofSpecs
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(proof.size, expandFrom = Alignment.Top) + fadeIn(proof.fade),
        exit = shrinkVertically(proof.size, shrinkTowards = Alignment.Top) + fadeOut(proof.fade),
    ) {
        content()
    }
}

@Composable
fun Modifier.hhPressScale(interactionSource: InteractionSource): Modifier {
    val motion = HhTheme.motion
    if (motion.reduced) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val scale: State<Float> = animateFloatAsState(
        targetValue = if (pressed) HH_PRESS_SCALE else 1f,
        animationSpec = motion.proofSpecs.spatialFast,
        label = "hhPressScale",
    )
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Stable
class HhListEnterState internal constructor(internal var open: Boolean)

@Composable
fun rememberHhListEnterState(): HhListEnterState {
    var played by rememberSaveable { mutableStateOf(false) }
    val state = remember { HhListEnterState(open = !played) }
    LaunchedEffect(state) {
        withFrameNanos { }
        state.open = false
        played = true
    }
    return state
}

@Composable
fun Modifier.hhListEnter(state: HhListEnterState, index: Int): Modifier {
    val motion = HhTheme.motion
    if (motion.reduced) return this
    val proof = motion.proofSpecs
    val distance = with(LocalDensity.current) { HhRise.toPx() }
    val delayMillis = index.coerceAtMost(proof.staggerMax - 1).coerceAtLeast(0) * proof.staggerMs.toLong()
    val progress = rememberEnterProgress(play = state.open, delayMillis = delayMillis)
    return graphicsLayer { rise(progress.value, distance) }
}

@Composable
private fun rememberEnterProgress(play: Boolean, delayMillis: Long = 0L): State<Float> {
    val spec = HhTheme.motion.proofSpecs.spatial
    val progress = remember { Animatable(if (play) 0f else 1f) }
    LaunchedEffect(progress) {
        if (progress.value < 1f) {
            if (coroutineContext[MotionDurationScale]?.scaleFactor != 0f) delay(delayMillis)
            progress.animateTo(targetValue = 1f, animationSpec = spec)
        }
    }
    return progress.asState()
}

private fun GraphicsLayerScope.rise(progress: Float, distance: Float) {
    alpha = progress.coerceIn(0f, 1f)
    translationY = (1f - progress) * distance
}

@Immutable
class HhNavTransitions internal constructor(
    private val proof: HhProofSpecs,
    private val reduced: Boolean,
    private val sink: Int,
    private val shift: Int,
) {
    fun forward(scope: AnimatedContentTransitionScope<*>, hierarchical: Boolean): ContentTransform {
        val enter = if (hierarchical && !reduced) {
            fadeIn(proof.fade) + slideInVertically(proof.offset) { sink }
        } else {
            fadeIn(proof.fade)
        }
        return with(scope) { enter togetherWith ExitTransition.KeepUntilTransitionsFinished }
    }

    fun back(hierarchical: Boolean): ContentTransform {
        val exit = if (hierarchical && !reduced) {
            fadeOut(proof.fade) + slideOutVertically(proof.offset) { sink }
        } else {
            fadeOut(proof.fade)
        }
        return EnterTransition.None togetherWith exit
    }

    fun tab(scope: AnimatedContentTransitionScope<*>, direction: Int, pop: Boolean): ContentTransform {
        val slide = if (reduced) EnterTransition.None else slideInHorizontally(proof.offset) { direction * shift }
        return with(scope) {
            if (pop) {
                slide togetherWith fadeOut(proof.fade)
            } else {
                fadeIn(proof.fade) + slide togetherWith ExitTransition.KeepUntilTransitionsFinished
            }
        }
    }
}

@Composable
fun rememberHhNavTransitions(): HhNavTransitions {
    val motion = HhTheme.motion
    val density = LocalDensity.current
    val sink = with(density) { HhSheetRise.roundToPx() }
    val towardEnd = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    val shift = with(density) { HhTabShift.roundToPx() } * towardEnd
    return remember(motion, sink, shift) { HhNavTransitions(motion.proofSpecs, motion.reduced, sink, shift) }
}
