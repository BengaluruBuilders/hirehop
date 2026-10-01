package com.hirehop.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSpotIllustration(
    kind: HhSpotKind,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    contentDescription: String? = null,
) {
    val spotColor = tint ?: HhTheme.colors.spotInk
    val description = contentDescription
    Canvas(
        modifier = modifier
            .size(width = HhSizeSpotWidth, height = HhSizeSpotHeight)
            .then(
                if (description == null) {
                    Modifier
                } else {
                    Modifier.semantics { this.contentDescription = description }
                },
            ),
    ) {
        drawSpotDocument(color = spotColor)
        when (kind) {
            HhSpotKind.Empty -> Unit
            HhSpotKind.Scanned -> drawSpotScanned(color = spotColor)
            HhSpotKind.Offline -> drawSpotOffline(color = spotColor)
            HhSpotKind.Error -> drawSpotError(color = spotColor)
            HhSpotKind.Done -> drawSpotDone(color = spotColor)
            HhSpotKind.Review -> drawSpotReview(color = spotColor)
        }
    }
}

private fun DrawScope.drawSpotDocument(color: Color) {
    val strokePx = HhWidthStroke.toPx()
    val half = strokePx / 2f
    val left = 6f + half
    val top = 4f + half
    val right = size.width - 6f - half
    val bottom = size.height - 6f - half
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = CornerRadius(4f),
        style = Stroke(width = strokePx),
    )
    val lineLeft = left + 8f
    val lineRight = right - 8f
    val height = bottom - top
    listOf(0.10f, 0.22f, 0.34f).forEach { fraction ->
        val y = top + height * fraction
        drawLine(
            color = color,
            start = Offset(lineLeft, y),
            end = Offset(lineRight, y),
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSpotScanned(color: Color) {
    val strokePx = HhWidthStroke.toPx()
    val arm = 7f
    val left = 3f
    val top = 1f
    val right = size.width - 3f
    val bottom = size.height - 1f
    val corners = listOf(
        listOf(Offset(left + arm, top), Offset(left, top), Offset(left, top + arm)),
        listOf(Offset(right - arm, top), Offset(right, top), Offset(right, top + arm)),
        listOf(Offset(left, bottom - arm), Offset(left, bottom), Offset(left + arm, bottom)),
        listOf(Offset(right, bottom - arm), Offset(right, bottom), Offset(right - arm, bottom)),
    )
    corners.forEach { corner ->
        val path = Path().apply {
            moveTo(corner[0].x, corner[0].y)
            lineTo(corner[1].x, corner[1].y)
            lineTo(corner[2].x, corner[2].y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

private fun DrawScope.drawSpotOffline(color: Color) {
    val strokePx = HhWidthStroke.toPx()
    val radius = 9f
    val center = spotMarkCenter()
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(width = strokePx),
    )
    val inset = radius * 0.5f
    drawLine(
        color = color,
        start = Offset(center.x - inset, center.y + inset),
        end = Offset(center.x + inset, center.y - inset),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawSpotError(color: Color) {
    val strokePx = HhWidthStroke.toPx()
    val center = spotMarkCenter()
    val barHalf = 5f
    drawLine(
        color = color,
        start = Offset(center.x, center.y - barHalf),
        end = Offset(center.x, center.y + barHalf * 0.35f),
        strokeWidth = strokePx * 1.4f,
        cap = StrokeCap.Round,
    )
    drawCircle(color = color, radius = strokePx, center = Offset(center.x, center.y + barHalf))
}

private fun DrawScope.drawSpotDone(color: Color) {
    val strokePx = HhWidthStroke.toPx()
    val center = spotMarkCenter()
    val path = Path().apply {
        moveTo(center.x - 8f, center.y)
        lineTo(center.x - 2f, center.y + 6f)
        lineTo(center.x + 8f, center.y - 6f)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawSpotReview(color: Color) {
    val strokePx = HhWidthStroke.toPx()
    val center = spotMarkCenter()
    val radius = 7f
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(center.x - 2f, center.y - 2f),
        style = Stroke(width = strokePx),
    )
    drawLine(
        color = color,
        start = Offset(center.x + 3f, center.y + 3f),
        end = Offset(center.x + 9f, center.y + 9f),
        strokeWidth = strokePx,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.spotMarkCenter(): Offset = Offset(size.width / 2f, size.height * 0.68f)

private val HhSpotIllustrationAllKinds: List<HhSpotKind> = HhSpotKind.entries

@Preview(showBackground = true)
@Composable
private fun HhSpotIllustrationPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhSpotIllustrationPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhSpotIllustrationDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhSpotIllustrationPreviewRow()
    }
}

@Composable
private fun HhSpotIllustrationPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        HhSpotIllustrationAllKinds.forEach { kind ->
            HhSpotIllustration(kind = kind)
        }
    }
}
