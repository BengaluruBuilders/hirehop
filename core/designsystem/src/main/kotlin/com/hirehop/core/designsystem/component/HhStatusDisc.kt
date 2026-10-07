package com.hirehop.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

internal fun HhColors.statusColor(kind: HhStatusKind): Color = when (kind) {
    HhStatusKind.Met -> met
    HhStatusKind.Partial -> partial
    HhStatusKind.Gap -> onSurface
}

@Composable
fun HhStatusDisc(
    kind: HhStatusKind,
    modifier: Modifier = Modifier,
    size: Dp = HhSizeStatusMark,
    contentDescription: String? = null,
) {
    val colors = HhTheme.colors
    HhStatusMark(kind, colors.statusColor(kind), colors.surface, modifier, size, contentDescription)
}

@Composable
internal fun HhStatusMark(
    kind: HhStatusKind,
    tint: Color,
    check: Color,
    modifier: Modifier,
    size: Dp,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription == null) {
        Modifier.clearAndSetSemantics {}
    } else {
        Modifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        scale(scale = this.size.minDimension / STATUS_VIEWPORT, pivot = Offset.Zero) {
            when (kind) {
                HhStatusKind.Met -> drawMet(tint, check)
                HhStatusKind.Partial -> drawPartial(tint)
                HhStatusKind.Gap -> drawGap(tint)
            }
        }
    }
}

private fun DrawScope.drawMet(tint: Color, check: Color) {
    drawCircle(color = tint, radius = 10f, center = Offset(12f, 12f))
    val path = Path().apply {
        moveTo(7.5f, 12.5f)
        lineTo(10.5f, 15.5f)
        lineTo(16.5f, 9f)
    }
    drawPath(path, check, style = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawPartial(tint: Color) {
    val topLeft = Offset(2.5f, 2.5f)
    val box = Size(19f, 19f)
    drawArc(tint, 90f, 180f, true, topLeft, box)
    drawArc(tint, 0f, 360f, false, topLeft, box, style = Stroke(width = 2f))
}

private fun DrawScope.drawGap(tint: Color) {
    val dashes = PathEffect.dashPathEffect(floatArrayOf(3.2f, 2.6f))
    drawArc(tint, 0f, 360f, false, Offset(2.5f, 2.5f), Size(19f, 19f), style = Stroke(width = 2f, pathEffect = dashes))
}

private const val STATUS_VIEWPORT = 24f

@Preview(showBackground = true)
@Composable
private fun HhStatusDiscPreview() {
    HhPreviewTheme(darkTheme = false) { HhStatusDiscPreviewRow() }
}

@Preview(showBackground = true)
@Composable
private fun HhStatusDiscDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhStatusDiscPreviewRow() }
}

@Composable
private fun HhStatusDiscPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhStatusKind.entries.forEach { kind -> HhStatusDisc(kind = kind) }
    }
}
