package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrEvidenceMark(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    tint: Color? = null,
) {
    val colors = TmrTheme.colors
    val fill = tint ?: colors.evidence
    val line = colors.evidenceLine
    val underline = TmrWidthUnderline
    Text(
        text = text,
        modifier = modifier
            .drawBehind {
                drawRect(color = fill)
                val y = size.height - underline.toPx() / 2f
                drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = underline.toPx())
            }
            .padding(horizontal = TmrTheme.spacing.xxs),
        style = style ?: TmrTheme.typography.bodyL,
        color = colors.onSurface,
    )
}

@Composable
fun evidenceMarkSpanStyle(): SpanStyle = SpanStyle(background = TmrTheme.colors.evidence)

@Composable
fun TmrEvidenceText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TmrTheme.typography.bodyL,
    color: Color = TmrTheme.colors.onSurface,
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val fill = TmrTheme.colors.evidence
    val line = TmrTheme.colors.evidenceLine
    val underline = TmrWidthUnderline
    BasicText(
        text = text,
        modifier = modifier.drawBehind {
            val result = layout ?: return@drawBehind
            text.spanStyles.filter { it.item.background == fill }.forEach { span ->
                result.underlineSegments(span.start, span.end).forEach { (from, to, y) ->
                    drawLine(line, Offset(from, y), Offset(to, y), strokeWidth = underline.toPx())
                }
            }
        },
        style = style.copy(color = color),
        onTextLayout = { layout = it },
    )
}

private fun TextLayoutResult.underlineSegments(start: Int, end: Int): List<Triple<Float, Float, Float>> {
    val firstLine = getLineForOffset(start)
    val lastLine = getLineForOffset((end - 1).coerceAtLeast(start))
    return (firstLine..lastLine).map { lineIndex ->
        val from = if (lineIndex == firstLine) getHorizontalPosition(start, true) else getLineLeft(lineIndex)
        val to = if (lineIndex == lastLine) getHorizontalPosition(end, true) else getLineRight(lineIndex)
        Triple(from, to, getLineBottom(lineIndex) - 1f)
    }
}

private const val TMR_EVIDENCE_MARK_SAMPLE_LINE = "Co-led"
private const val TMR_EVIDENCE_MARK_SAMPLE_TAIL = " the move of 6 payment screens to Jetpack Compose."

@Preview(showBackground = true)
@Composable
private fun TmrEvidenceMarkPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrEvidenceMarkPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun TmrEvidenceMarkDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrEvidenceMarkPreviewColumn() }
}

@Composable
private fun TmrEvidenceMarkPreviewColumn() {
    Column(modifier = Modifier.padding(TmrTheme.spacing.lg)) {
        TmrEvidenceMark(text = TMR_EVIDENCE_MARK_SAMPLE_LINE)
        TmrEvidenceText(
            text = buildAnnotatedString {
                withStyle(evidenceMarkSpanStyle().copy(fontWeight = FontWeight.SemiBold)) {
                    append(TMR_EVIDENCE_MARK_SAMPLE_LINE)
                }
                append(TMR_EVIDENCE_MARK_SAMPLE_TAIL)
            },
        )
    }
}
