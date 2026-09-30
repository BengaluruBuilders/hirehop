package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.theme.HhTheme

private enum class HhApplicationDotShape { Ring, DoubleRing, HalfRing, Solid, Diagonal, Bar }

@Composable
fun HhApplicationStatusChip(
    kind: HhApplicationStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
    dotSize: Dp = HhSizeDisc,
) {
    val colors = HhTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(HhTheme.shapes.full),
        color = colors.surface,
        border = BorderStroke(width = HhWidthHairline, color = colors.hairlineStrong),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = HhTheme.spacing.sm,
                vertical = HhTheme.spacing.xxs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            HhApplicationStatusDot(
                shape = kind.dotShape(),
                color = colors.onSurfaceVariant,
                size = dotSize,
            )
            Text(
                text = label ?: kind.defaultLabel(),
                style = HhTheme.typography.labelMedium,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun HhApplicationStatusDot(
    shape: HhApplicationDotShape,
    color: Color,
    size: Dp,
) {
    Canvas(modifier = Modifier.size(size)) {
        drawHhApplicationDot(shape = shape, color = color)
    }
}

private fun DrawScope.drawHhApplicationDot(shape: HhApplicationDotShape, color: Color) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    when (shape) {
        HhApplicationDotShape.Ring -> drawCircle(
            color = color,
            radius = radius - HhWidthHairline.toPx() / 2f,
            center = center,
            style = Stroke(width = HhWidthHairline.toPx()),
        )
        HhApplicationDotShape.HalfRing -> {
            val inner = radius - HhWidthHairline.toPx()
            drawCircle(
                color = color,
                radius = radius - HhWidthHairline.toPx() / 2f,
                center = center,
                style = Stroke(width = HhWidthHairline.toPx()),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - inner, center.y - inner),
                size = Size(inner * 2f, inner * 2f),
                style = Fill,
            )
        }
        HhApplicationDotShape.DoubleRing -> {
            drawCircle(
                color = color,
                radius = radius - HhWidthHairline.toPx() / 2f,
                center = center,
                style = Stroke(width = HhWidthHairline.toPx()),
            )
            drawCircle(
                color = color,
                radius = radius / 2f,
                center = center,
                style = Stroke(width = HhWidthHairline.toPx()),
            )
        }
        HhApplicationDotShape.Solid -> drawCircle(color = color, radius = radius, center = center)
        HhApplicationDotShape.Bar -> {
            drawCircle(
                color = color,
                radius = radius - HhWidthHairline.toPx() / 2f,
                center = center,
                style = Stroke(width = HhWidthHairline.toPx()),
            )
            drawLine(
                color = color,
                start = Offset(center.x - radius / 2f, center.y),
                end = Offset(center.x + radius / 2f, center.y),
                strokeWidth = HhWidthHairline.toPx(),
            )
        }
        HhApplicationDotShape.Diagonal -> {
            drawCircle(
                color = color,
                radius = radius - HhWidthHairline.toPx() / 2f,
                center = center,
                style = Stroke(width = HhWidthHairline.toPx()),
            )
            drawLine(
                color = color,
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius),
                strokeWidth = HhWidthHairline.toPx(),
            )
        }
    }
}

private fun HhApplicationStatusKind.dotShape(): HhApplicationDotShape =
    when (this) {
        HhApplicationStatusKind.Saved -> HhApplicationDotShape.Ring
        HhApplicationStatusKind.Applied -> HhApplicationDotShape.Solid
        HhApplicationStatusKind.Interview -> HhApplicationDotShape.HalfRing
        HhApplicationStatusKind.Offer -> HhApplicationDotShape.Diagonal
        HhApplicationStatusKind.Rejected -> HhApplicationDotShape.Bar
        HhApplicationStatusKind.NoResponse -> HhApplicationDotShape.DoubleRing
    }

private val HhApplicationStatusChipAllKinds: List<HhApplicationStatusKind> =
    HhApplicationStatusKind.entries

@Preview(showBackground = true)
@Composable
private fun HhApplicationStatusChipPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhApplicationStatusChipPreviewColumn()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhApplicationStatusChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhApplicationStatusChipPreviewColumn()
    }
}

@Composable
private fun HhApplicationStatusChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhApplicationStatusChipAllKinds.forEach { kind ->
            HhApplicationStatusChip(kind = kind)
        }
    }
}
