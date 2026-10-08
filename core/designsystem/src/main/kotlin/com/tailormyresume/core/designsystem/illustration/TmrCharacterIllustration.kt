package com.tailormyresume.core.designsystem.illustration

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrCharacterIllustration(
    illustration: TmrIllustration,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = TmrTheme.colors
    val accent = when (illustration) {
        TmrIllustration.Error -> colors.error
        TmrIllustration.Offline, TmrIllustration.Goodbye -> colors.onSurfaceVariant
        else -> colors.brand
    }
    val onAccent = when (illustration) {
        TmrIllustration.Error -> colors.onError
        TmrIllustration.Offline, TmrIllustration.Goodbye -> if (TmrTheme.isDark) colors.header else colors.onHeader
        else -> colors.onBrand
    }
    val semanticsModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics {
            this.contentDescription = contentDescription
        }
    }
    Canvas(semanticsModifier) {
        drawDocument(colors.document, colors.onSurfaceVariant, colors.outlineVariant)
        drawBadge(illustration, accent, onAccent)
    }
}

private fun DrawScope.drawDocument(fill: Color, ink: Color, border: Color) {
    val edge = size.minDimension
    val cardTop = Offset(size.width * 0.13f, size.height * 0.1f)
    val cardSize = Size(size.width * 0.64f, size.height * 0.78f)
    val radius = CornerRadius(edge * 0.09f)
    drawRoundRect(fill, cardTop, cardSize, radius)
    drawRoundRect(border, cardTop, cardSize, radius, style = Stroke(width = edge * 0.012f))
    listOf(0.29f, 0.43f, 0.57f).forEachIndexed { index, fraction ->
        drawRoundRect(
            color = ink.copy(alpha = if (index == 0) 0.75f else 0.45f),
            topLeft = Offset(size.width * 0.24f, size.height * fraction),
            size = Size(size.width * if (index == 2) 0.3f else 0.4f, edge * 0.016f),
            cornerRadius = CornerRadius(edge * 0.008f),
        )
    }
}

private fun DrawScope.drawBadge(illustration: TmrIllustration, accent: Color, onAccent: Color) {
    val edge = size.minDimension
    val center = Offset(size.width * 0.72f, size.height * 0.76f)
    val radius = edge * 0.19f
    drawCircle(color = accent, radius = radius, center = center)
    val stroke = edge * 0.028f
    if (illustration == TmrIllustration.Error) {
        drawLine(onAccent, center + Offset(0f, -radius * 0.45f), center + Offset(0f, radius * 0.15f), stroke)
        drawCircle(onAccent, radius = stroke * 0.52f, center = center + Offset(0f, radius * 0.46f))
    } else if (illustration == TmrIllustration.Offline || illustration == TmrIllustration.Goodbye) {
        drawLine(onAccent, center + Offset(-radius * 0.4f, 0f), center + Offset(radius * 0.4f, 0f), stroke)
    } else {
        drawLine(onAccent, center + Offset(-radius * 0.45f, 0f), center + Offset(-radius * 0.1f, radius * 0.3f), stroke)
        drawLine(onAccent, center + Offset(-radius * 0.1f, radius * 0.3f), center + Offset(radius * 0.5f, -radius * 0.36f), stroke)
    }
}
