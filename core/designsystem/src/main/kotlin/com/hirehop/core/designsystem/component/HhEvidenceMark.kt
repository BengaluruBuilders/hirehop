package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhEvidenceMark(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    tint: Color? = null,
) {
    val markTint = tint ?: HhTheme.colors.evidence
    val markStyle = style ?: HhTheme.typography.bodyLarge
    Text(
        text = text,
        modifier = modifier
            .background(color = markTint, shape = RoundedCornerShape(HhTheme.shapes.xs))
            .padding(horizontal = HhTheme.spacing.xxs),
        style = markStyle,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
fun evidenceMarkSpanStyle(): SpanStyle =
    SpanStyle(
        background = HhTheme.colors.evidence,
        fontWeight = FontWeight.Medium,
    )

private const val HH_EVIDENCE_MARK_SAMPLE_LINE = "Wrote weekly SQL reports in PostgreSQL"
private const val HH_EVIDENCE_MARK_SAMPLE_TAIL = " for the operations team."

@Preview(showBackground = true)
@Composable
private fun HhEvidenceMarkPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhEvidenceMarkPreviewColumn()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhEvidenceMarkDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhEvidenceMarkPreviewColumn()
    }
}

@Composable
private fun HhEvidenceMarkPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = HH_EVIDENCE_MARK_SAMPLE_LINE + HH_EVIDENCE_MARK_SAMPLE_TAIL,
            style = HhTheme.typography.bodyLarge,
        )
        HhEvidenceMark(text = HH_EVIDENCE_MARK_SAMPLE_LINE)
    }
}
