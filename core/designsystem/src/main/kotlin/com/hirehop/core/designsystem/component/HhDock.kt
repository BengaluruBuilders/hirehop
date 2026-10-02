package com.hirehop.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow
import kotlin.math.roundToInt

@Composable
fun HhDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val shape = HhTheme.shapes.pill
    Surface(
        modifier = modifier
            .widthIn(max = HhMaxWidthDock)
            .fillMaxWidth()
            .hhShadow(HhTheme.elevation.dock, shape),
        shape = shape,
        color = colors.tool,
        contentColor = colors.onTool,
        border = if (HhTheme.isDark) BorderStroke(HhWidthHairline, colors.outlineSoft) else null,
    ) {
        val spans = remember { mutableStateListOf<HhDockSpan>() }
        val pillColor = colors.primary
        CompositionLocalProvider(LocalHhDockSpans provides spans) {
            Row(
                modifier = Modifier
                    .height(HhHeightDock)
                    .padding(horizontal = HhTheme.spacing.sm)
                    .drawBehind { drawSelectionPill(spans, pillColor) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

private class HhDockSpan(val selection: State<Float>) {
    var bounds by mutableStateOf(Rect.Zero)
}

private val LocalHhDockSpans = compositionLocalOf<SnapshotStateList<HhDockSpan>> {
    error("HhDockItem must be inside HhDock")
}

private fun DrawScope.drawSelectionPill(spans: List<HhDockSpan>, color: Color) {
    var left = 0f
    var top = 0f
    var right = 0f
    var bottom = 0f
    spans.forEach { span ->
        val weight = span.selection.value
        left += span.bounds.left * weight
        top += span.bounds.top * weight
        right += span.bounds.right * weight
        bottom += span.bounds.bottom * weight
    }
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = CornerRadius((bottom - top) / 2),
    )
}

private fun Modifier.revealWidth(fraction: () -> Float): Modifier = this
    .clipToBounds()
    .graphicsLayer { alpha = (fraction() * 2 - 1).coerceIn(0f, 1f) }
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0))
        val width = (placeable.width * fraction().coerceIn(0f, 1f)).roundToInt()
        layout(width, placeable.height) { placeable.placeRelative(0, 0) }
    }

@Composable
fun HhDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: (@Composable () -> Unit)? = null,
) {
    val colors = HhTheme.colors
    val motion = HhTheme.motion.proofSpecs
    val selection = animateFloatAsState(if (selected) 1f else 0f, motion.spatial, label = "dockSelection")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.92f else 1f, motion.spatialFast, label = "dockPress")
    val spans = LocalHhDockSpans.current
    val span = remember { HhDockSpan(selection) }
    DisposableEffect(spans) {
        spans.add(span)
        onDispose { spans.remove(span) }
    }
    val labelled = if (label != null) selection.value else 0f
    Row(
        modifier = modifier
            .onPlaced { span.bounds = it.boundsInParent() }
            .defaultMinSize(minWidth = HhHeightTouch, minHeight = HhHeightTouch)
            .clip(HhTheme.shapes.pill)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(horizontal = lerp(HhDockIconInset, 18.dp, labelled)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = lerp(colors.onToolVariant, colors.onPrimary, selection.value)
        CompositionLocalProvider(LocalContentColor provides tint) {
            Box(Modifier.size(HhDockIconSize), contentAlignment = Alignment.Center) { icon() }
            if (label != null) {
                Row(Modifier.revealWidth { selection.value }) {
                    Spacer(Modifier.width(HhTheme.spacing.sm))
                    ProvideTextStyle(HhTheme.typography.labelL) { label() }
                }
            }
        }
    }
}

private val HhDockIconSize = HhSizeIcon + 2.dp
private val HhDockIconInset = (HhHeightTouch - HhDockIconSize) / 2

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
    HhDock(modifier = Modifier.padding(HhTheme.spacing.gutter)) {
        HhDockItem(
            selected = true,
            onClick = {},
            contentDescription = "Home",
            icon = { HhDockIcon(HhIcons.Home) },
            label = { Text("Home") },
        )
        HhDockItem(selected = false, onClick = {}, contentDescription = "Applications", icon = { HhDockIcon(HhIcons.Applications) })
        HhDockItem(selected = false, onClick = {}, contentDescription = "Facts", icon = { HhDockIcon(HhIcons.Facts) })
        HhDockItem(selected = false, onClick = {}, contentDescription = "Profile", icon = { HhDockIcon(HhIcons.Profile) })
    }
}
