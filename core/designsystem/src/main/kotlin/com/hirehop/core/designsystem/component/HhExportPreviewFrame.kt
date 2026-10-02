package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow

object HhPaperColors {
    val Page = Color(0xFFFFFFFF)
    val Ink = Color(0xFF111111)
    val Body = Color(0xFF444444)
    val Rule = Color(0xFF999999)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HhExportPreviewFrame(
    meta: String,
    caption: String,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    paper: @Composable ColumnScope.() -> Unit,
) {
    val colors = HhTheme.colors
    HhCardSurface(
        modifier = modifier,
        shape = HhTheme.shapes.heroCard,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        trailingAction = null,
        fill = colors.ground,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.cardPadding),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = meta, style = HhTheme.typography.labelM, color = colors.onSurfaceVariant)
                if (badge != null) {
                    badge()
                }
            }
            HhPaperPage(content = paper)
            Text(
                text = caption,
                style = HhTheme.typography.bodyS,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HhPaperPage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .background(HhTheme.colors.card, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(HhTheme.spacing.cardPadding),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .fillMaxWidth()
                .aspectRatio(PAPER_ASPECT)
                .hhShadow(HhTheme.elevation.level2, androidx.compose.ui.graphics.RectangleShape)
                .background(HhPaperColors.Page)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content,
        )
    }
}

private const val PAPER_ASPECT = 260f / 368f

@Preview(showBackground = true)
@Composable
private fun HhExportPreviewFramePreview() {
    HhPreviewTheme(darkTheme = false) { HhExportPreviewFrameSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhExportPreviewFrameDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhExportPreviewFrameSample() }
}

@Composable
private fun HhExportPreviewFrameSample() {
    HhExportPreviewFrame(
        meta = "PDF · A4 · 1 page · plain template",
        caption = "The export is black on white, single column, no jade.",
        modifier = Modifier.padding(HhTheme.spacing.gutter),
        badge = { HhTrustChip(kind = HhTrustKind.NeverInvents, label = "Never invents") },
    ) {
        Text(text = "Priya Deshmukh", style = HhTheme.typography.titleS, color = HhPaperColors.Ink)
        Text(text = "Pune, Maharashtra", style = HhTheme.typography.labelM, color = HhPaperColors.Body)
    }
}
