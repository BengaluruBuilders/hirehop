package com.hirehop.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class HhHeaderData(val drawsAboveContent: Boolean, val overlap: Dp)

internal fun Modifier.hhHeaderBackdrop(
    fill: Color,
    ring: Color,
    squiggle: Color,
    decorationTop: Dp,
    sheetCutout: Dp?,
    bottomRadius: Dp = 0.dp,
): Modifier = drawBehind {
    val bounds = Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
    val visible = if (sheetCutout == null) bounds else bounds.minus(sheetShape(sheetCutout.toPx()))
    val radius = bottomRadius.toPx()
    clipPath(visible) {
        drawRoundRect(fill, size = size, cornerRadius = CornerRadius(radius))
        drawRect(fill, size = Size(size.width, size.height - radius))
        drawDecorations(ring, squiggle, decorationTop.toPx())
    }
}

private fun DrawScope.sheetShape(radius: Float): Path {
    val top = size.height - radius
    return Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(0f, top, size.width, size.height + radius),
                topLeft = CornerRadius(radius),
                topRight = CornerRadius(radius),
            ),
        )
    }
}

private fun Path.minus(other: Path): Path = Path.combine(PathOperation.Difference, this, other)

private fun DrawScope.drawDecorations(ring: Color, squiggle: Color, top: Float) {
    val stroke = DECORATION_STROKE.toPx()
    val ringSize = RING_SIZE.toPx()
    val squiggleWidth = SQUIGGLE_WIDTH.toPx()
    drawDecoration(
        HhDecorationKind.Ring,
        ring,
        Offset(size.width - RING_RIGHT.toPx() - ringSize, top),
        ringSize,
        stroke,
    )
    drawDecoration(
        HhDecorationKind.Squiggle,
        squiggle,
        Offset(size.width - SQUIGGLE_RIGHT.toPx() - squiggleWidth, top + SQUIGGLE_DROP.toPx()),
        squiggleWidth,
        stroke,
    )
}

private val DECORATION_STROKE = 2.6.dp
private val RING_SIZE = 34.dp
private val RING_RIGHT = 22.dp
private val SQUIGGLE_WIDTH = 54.dp
private val SQUIGGLE_RIGHT = 70.dp
private val SQUIGGLE_DROP = 50.dp
