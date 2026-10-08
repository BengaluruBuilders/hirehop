package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.illustration.TmrCharacterIllustration
import com.tailormyresume.core.designsystem.illustration.TmrIllustration
import com.tailormyresume.core.designsystem.theme.TmrTheme

private val TmrSpotDefaultSize = 160.dp

@Composable
fun TmrSpotIllustration(
    kind: TmrSpotKind,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    contentDescription: String? = null,
) {
    val sized = Modifier.size(TmrSpotDefaultSize).then(modifier)
    val illustration = kind.illustration()
    if (illustration != null) {
        TmrCharacterIllustration(
            illustration = illustration,
            modifier = sized,
            contentDescription = contentDescription,
        )
        return
    }
    val glyphColor = tint ?: TmrTheme.colors.onSurfaceVariant
    Canvas(
        modifier = sized.then(
            if (contentDescription == null) {
                Modifier
            } else {
                Modifier.semantics { this.contentDescription = contentDescription }
            },
        ),
    ) {
        drawDocumentGlyph(color = glyphColor)
    }
}

private fun DrawScope.drawDocumentGlyph(color: Color) {
    val strokePx = size.minDimension * 0.03f
    val left = size.width * 0.28f
    val top = size.height * 0.2f
    val width = size.width * 0.44f
    val height = size.height * 0.6f
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(size.minDimension * 0.04f),
        style = Stroke(width = strokePx),
    )
    listOf(0.25f, 0.4f, 0.55f).forEach { fraction ->
        val y = top + height * fraction
        drawLine(
            color = color,
            start = Offset(left + width * 0.2f, y),
            end = Offset(left + width * 0.8f, y),
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TmrSpotIllustrationPreview() {
    TmrPreviewTheme(darkTheme = false) {
        TmrSpotIllustrationPreviewContent()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF15181C)
@Composable
private fun TmrSpotIllustrationDarkPreview() {
    TmrPreviewTheme(darkTheme = true) {
        TmrSpotIllustrationPreviewContent()
    }
}

@Composable
private fun TmrSpotIllustrationPreviewContent() {
    FlowRow(
        modifier = Modifier.padding(TmrTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrSpotKind.entries.forEach { kind -> TmrSpotIllustration(kind = kind) }
        TmrCharacterIllustration(
            illustration = TmrIllustration.Hero,
            modifier = Modifier.size(width = 174.dp, height = 200.dp),
        )
    }
}
