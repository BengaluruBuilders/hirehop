package com.hirehop.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhStatusDisc(
    kind: HhStatusKind,
    modifier: Modifier = Modifier,
    size: Dp = HhSizeDisc,
    contentDescription: String? = null,
) {
    val tint = hhStatusTint(kind)
    val description = contentDescription
    Canvas(
        modifier = modifier
            .size(size)
            .then(
                if (description == null) {
                    Modifier
                } else {
                    Modifier.semantics { this.contentDescription = description }
                },
            ),
    ) {
        drawHhStatusDisc(kind = kind, tint = tint)
    }
}

@Composable
private fun hhStatusTint(kind: HhStatusKind): Color =
    when (kind) {
        HhStatusKind.Met -> HhTheme.colors.success
        HhStatusKind.Partial -> HhTheme.colors.warning
        HhStatusKind.Gap -> HhTheme.colors.gap
    }

private fun DrawScope.drawHhStatusDisc(kind: HhStatusKind, tint: Color) {
    val strokePx = HhWidthStroke.toPx()
    val hairlinePx = HhWidthHairline.toPx()
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    when (kind) {
        HhStatusKind.Met -> drawCircle(color = tint, radius = radius, center = center)
        HhStatusKind.Partial -> {
            drawCircle(
                color = tint,
                radius = radius - strokePx / 2f,
                center = center,
                style = Stroke(width = strokePx),
            )
            val inner = radius - strokePx
            drawArc(
                color = tint,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - inner, center.y - inner),
                size = Size(inner * 2f, inner * 2f),
                style = Fill,
            )
        }
        HhStatusKind.Gap -> {
            drawCircle(
                color = tint,
                radius = radius - hairlinePx / 2f,
                center = center,
                style = Stroke(width = hairlinePx),
            )
            val arm = radius * 0.6f
            drawLine(
                color = tint,
                start = Offset(center.x - arm, center.y),
                end = Offset(center.x + arm, center.y),
                strokeWidth = hairlinePx,
            )
            drawLine(
                color = tint,
                start = Offset(center.x, center.y - arm),
                end = Offset(center.x, center.y + arm),
                strokeWidth = hairlinePx,
            )
        }
    }
}

private val HhStatusDiscAllKinds: List<HhStatusKind> = HhStatusKind.entries

@Preview(showBackground = true)
@Composable
private fun HhStatusDiscPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhStatusDiscPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhStatusDiscDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhStatusDiscPreviewRow()
    }
}

@Composable
private fun HhStatusDiscPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhStatusDiscAllKinds.forEach { kind ->
            HhStatusDisc(kind = kind, size = HhTheme.spacing.d20)
        }
    }
}
