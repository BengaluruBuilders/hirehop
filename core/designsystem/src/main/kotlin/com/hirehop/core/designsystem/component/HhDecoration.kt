package com.hirehop.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhDecorationKind(
    internal val width: Float,
    internal val height: Float,
    internal val pathData: String?,
) {
    Squiggle(64f, 18f, "M2 11C7 3 12 3 16 9s9 7 13 1 9-7 13-1 9 7 13 1 6-5 7-4"),
    Ring(30f, 30f, "M15 4c6.5 0 11 4.6 11 10.8S21.3 26 15.2 26 4 21.5 4 15.3C4 9.6 8 5.6 13 4.6"),
    Dots(40f, 14f, null),
    Spark(30f, 30f, "M7 12L3 6M15 9V2M23 12l4-6"),
    Loop(60f, 30f, "M3 24c8-2 12-8 10-14s-9-3-6 4 14 8 20 2 6-12 0-12-6 10 2 14 16 2 22-6"),
    Zigzag(48f, 16f, "M2 12l7-8 7 8 7-8 7 8 7-8 7 8"),
    Plus(18f, 18f, "M9 2v14M2 9h14"),
}

private val DotCenters = listOf(6f, 20f, 34f)
private const val DOT_RADIUS = 3.5f

internal fun DrawScope.drawDecoration(
    kind: HhDecorationKind,
    color: Color,
    topLeft: Offset,
    width: Float,
    strokeWidth: Float,
) {
    val scale = width / kind.width
    withTransform({
        translate(topLeft.x, topLeft.y)
        scale(scale, scale, pivot = Offset.Zero)
    }) {
        val data = kind.pathData
        if (data == null) {
            DotCenters.forEach { drawCircle(color, DOT_RADIUS, Offset(it, kind.height / 2)) }
        } else {
            drawPath(
                PathParser().parsePathString(data).toPath(),
                color,
                style = Stroke(strokeWidth / scale, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
fun HhDecoration(
    kind: HhDecorationKind,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.6.dp,
) {
    Canvas(modifier = modifier.size(kind.width.dp, kind.height.dp)) {
        drawDecoration(kind, color, Offset.Zero, size.width, strokeWidth.toPx())
    }
}

@Preview(showBackground = true)
@Composable
private fun HhDecorationPreview() {
    HhPreviewTheme(darkTheme = false) { HhDecorationPreviewRow() }
}

@Composable
private fun HhDecorationPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhDecorationKind.entries.forEach { HhDecoration(it, HhTheme.colors.coral) }
    }
}
