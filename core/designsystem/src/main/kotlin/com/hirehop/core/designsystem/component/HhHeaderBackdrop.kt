package com.hirehop.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
    circles: Color,
    smallCircleTop: Dp,
    sheetCutout: Dp?,
): Modifier = drawBehind {
    val bounds = Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
    val visible = if (sheetCutout == null) bounds else bounds.minus(sheetShape(sheetCutout.toPx()))
    clipPath(visible) {
        drawRect(fill)
        drawCircles(circles, smallCircleTop.toPx())
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

private fun DrawScope.drawCircles(color: Color, smallTop: Float) {
    val large = HhSizeCircleLarge.toPx()
    val small = HhSizeCircleSmall.toPx()
    val inset = CIRCLE_INSET.toPx()
    drawCircle(color, large / 2f, Offset(size.width + inset - large / 2f, -CIRCLE_RISE.toPx() + large / 2f))
    drawCircle(color, small / 2f, Offset(-CIRCLE_LEFT.toPx() + small / 2f, smallTop + small / 2f))
}

private val CIRCLE_INSET = 70.dp
private val CIRCLE_RISE = 90.dp
private val CIRCLE_LEFT = 60.dp
