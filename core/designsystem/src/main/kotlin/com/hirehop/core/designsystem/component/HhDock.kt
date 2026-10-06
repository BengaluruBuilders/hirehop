package com.hirehop.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhMotion
import com.hirehop.core.designsystem.theme.HhTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val HhDockBallSize = 64.dp
private val HhDockNotchHalfWidth = 56.dp
private val HhDockNotchDepth = 37.dp
private val HhDockShadowStrokes = listOf(36.dp, 24.dp, 12.dp)
private val HhDockShadowColor = Color(0x1016181D)
private val HhDockIconRise = HhDockDefaults.height / 2

@Composable
fun HhDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val motion = HhTheme.motion
    val barColor = colors.tool
    val ballColor = colors.brand
    val edgeColor = if (HhTheme.isDark) colors.outlineSoft else null
    val state = remember { HhDockState() }
    val path = remember { Path() }
    val reach = with(LocalDensity.current) { HhDockNotchHalfWidth.toPx() }
    LaunchedEffect(state, motion, reach) {
        snapshotFlow {
            state.spans.firstOrNull { it.selected.value && !it.centerX.isNaN() }?.let { it to it.centerX }
        }.filterNotNull().collectLatest { (span, centerX) -> state.moveTo(span, centerX, reach, motion) }
    }
    CompositionLocalProvider(
        LocalHhDockState provides state,
        LocalContentColor provides colors.onTool,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                .drawBehind { drawNotchedBar(state, path, barColor, ballColor, edgeColor) },
        ) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .padding(top = HhDockDefaults.ballOverhang)
                    .pointerInput(Unit) {},
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .height(HhDockDefaults.ballOverhang + HhHeightDock),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
                content = content,
            )
        }
    }
}

internal class HhDockSpan(val selected: State<Boolean>) {
    var centerX by mutableFloatStateOf(Float.NaN)
}

internal class HhDockState {
    val spans = mutableStateListOf<HhDockSpan>()
    val notchX = Animatable(0f)
    val lift = Animatable(0f)
    var current by mutableStateOf<HhDockSpan?>(null)
        private set

    fun liftAt(centerX: Float, reach: Float): Float {
        val distance = abs(notchX.value - centerX)
        return if (distance < reach) lift.value * (1f - distance / reach) else 0f
    }

    suspend fun moveTo(span: HhDockSpan, centerX: Float, reach: Float, motion: HhMotion) {
        val travels = current != null && current !== span && !motion.reduced
        current = span
        if (!travels) {
            notchX.snapTo(centerX)
            lift.snapTo(1f)
            return
        }
        coroutineScope {
            launch { lift.animateTo(0f, motion.proofSpecs.fade) }
            snapshotFlow { lift.value < 0.5f }.first { it }
            launch { notchX.animateTo(centerX, motion.proofSpecs.travel) }
            snapshotFlow { abs(notchX.value - centerX) < reach }.first { it }
            lift.animateTo(1f, motion.proofSpecs.spatial)
        }
    }
}

private val LocalHhDockState = compositionLocalOf<HhDockState> {
    error("HhDockItem must be inside HhDock")
}

private fun DrawScope.drawNotchedBar(
    state: HhDockState,
    path: Path,
    barColor: Color,
    ballColor: Color,
    edgeColor: Color?,
) {
    val barTop = HhDockDefaults.ballOverhang.toPx()
    val placed = state.current != null
    val cx = state.notchX.value
    val lift = state.lift.value
    val depth = HhDockNotchDepth.toPx()
    path.rewind()
    val corner = HhRadiusSheet.toPx()
    path.moveTo(0f, barTop + corner)
    path.arcTo(Rect(0f, barTop, corner * 2, barTop + corner * 2), 180f, 90f, false)
    if (placed) {
        val w = HhDockNotchHalfWidth.toPx()
        val d = depth * lerp(0.6f, 1f, lift)
        val start = max(cx - w, corner)
        val end = min(cx + w, size.width - corner)
        path.lineTo(start, barTop)
        path.cubicTo(start + (cx - start) * 0.5f, barTop, cx - w * 0.55f, barTop + d, cx, barTop + d)
        path.cubicTo(cx + w * 0.55f, barTop + d, end - (end - cx) * 0.5f, barTop, end, barTop)
    }
    path.lineTo(size.width - corner, barTop)
    path.arcTo(Rect(size.width - corner * 2, barTop, size.width, barTop + corner * 2), 270f, 90f, false)
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
    path.close()
    HhDockShadowStrokes.forEach { width ->
        drawPath(path, HhDockShadowColor, style = Stroke(width.toPx()))
    }
    if (placed) {
        drawCircle(
            color = ballColor,
            radius = HhDockBallSize.toPx() / 2 * lerp(0.5f, 1f, lift),
            center = Offset(cx, barTop + depth * (1f - lift)),
        )
    }
    drawPath(path, barColor)
    if (edgeColor != null) {
        drawPath(path, edgeColor, style = Stroke(HhWidthHairline.toPx()))
    }
}

@Composable
fun HhDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val colors = HhTheme.colors
    val motion = HhTheme.motion.proofSpecs
    val selectedState = rememberUpdatedState(selected)
    val state = LocalHhDockState.current
    val span = remember { HhDockSpan(selectedState) }
    DisposableEffect(state) {
        state.spans.add(span)
        onDispose { state.spans.remove(span) }
    }
    val reach = with(LocalDensity.current) { HhDockNotchHalfWidth.toPx() }
    val overBall by remember(state, span, reach) {
        derivedStateOf { ((state.liftAt(span.centerX, reach) - 0.2f) / 0.3f).coerceIn(0f, 1f) }
    }
    val tint = lerp(colors.onToolVariant, colors.onBrand, overBall)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.92f else 1f, motion.spatialFast, label = "dockPress")
    Box(
        modifier = modifier
            .onPlaced { span.centerX = it.boundsInParent().center.x }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationY = -HhDockIconRise.toPx() * state.liftAt(span.centerX, reach)
            }
            .padding(bottom = (HhHeightDock - HhHeightTouch) / 2)
            .size(HhHeightTouch)
            .clip(CircleShape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) { icon() }
    }
}

private val HhDockIconSize = HhSizeIcon + 2.dp

@Composable
fun HhDockIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(HhDockIconSize))
}

@Preview(showBackground = true)
@Composable
private fun HhDockPreview() {
    HhPreviewTheme(darkTheme = false) { HhDockSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhDockDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhDockSample() }
}

@Composable
private fun HhDockSample() {
    HhDock {
        HhDockItem(
            selected = true,
            onClick = {},
            contentDescription = "Applications",
            icon = { HhDockIcon(HhIcons.Applications) },
        )
        HhDockItem(
            selected = false,
            onClick = {},
            contentDescription = "Profile",
            icon = { HhDockIcon(HhIcons.Profile) },
        )
        HhDockItem(
            selected = false,
            onClick = {},
            contentDescription = "Facts",
            icon = { HhDockIcon(HhIcons.Facts) },
        )
    }
}
