package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhPaperColors
import com.hirehop.core.designsystem.theme.HhTheme

private const val PAPER_NAME_SIZE = 13f
private const val PAPER_BODY_SIZE = 7.6f
private const val PAPER_HEADING_SIZE = 7.4f
private const val PAPER_LINE_RATIO = 1.4f
private const val PAPER_ROW_GAP = 3f
private const val PAPER_HEADING_TOP = 9f
private const val PAPER_HEADING_BOTTOM = 5f
private const val PAPER_RULE_THICKNESS = 0.6f
private const val PAPER_HEADING_TRACKING = 0.08f
private const val BULLET_PREFIX = "• "
private const val DETAIL_SEPARATOR = " · "

@Composable
internal fun ExportPaper(
    sheet: ExportPreviewSheet,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    val metrics = PaperMetrics()
    Column(modifier = modifier.fillMaxWidth().verticalScroll(scrollState)) {
        Text(text = sheet.name, style = metrics.name(), color = HhPaperColors.Ink)
        Spacer(Modifier.height(metrics.rowGap))
        PaperLine(text = sheet.contactLine, metrics = metrics)
        PaperLine(text = sheet.headline, metrics = metrics)
        sheet.sections.forEach { section ->
            PaperHeading(text = section.heading, metrics = metrics)
            section.entries.forEach { entry ->
                val title = listOf(entry.title, entry.organization, entry.dateRange)
                    .filter { part -> part.isNotBlank() }
                    .joinToString(DETAIL_SEPARATOR)
                PaperLine(text = title, metrics = metrics, bold = true)
                entry.bullets.forEach { bullet -> PaperLine(text = BULLET_PREFIX + bullet, metrics = metrics) }
            }
        }
        if (sheet.skills.isNotEmpty()) {
            PaperHeading(text = sheet.skillsHeading, metrics = metrics)
            PaperLine(text = sheet.skills.joinToString(DETAIL_SEPARATOR), metrics = metrics)
        }
    }
}

@Composable
private fun PaperLine(text: String, metrics: PaperMetrics, bold: Boolean = false) {
    if (text.isBlank()) return
    Text(
        text = text,
        style = metrics.body(bold = bold),
        color = if (bold) HhPaperColors.Ink else HhPaperColors.Body,
        modifier = Modifier.padding(bottom = metrics.rowGap),
    )
}

@Composable
private fun PaperHeading(text: String, metrics: PaperMetrics) {
    Column(modifier = Modifier.padding(PaddingValues(top = metrics.headingTop, bottom = metrics.headingBottom))) {
        Text(text = text.uppercase(), style = metrics.heading(), color = HhPaperColors.Ink)
        HhDivider(thickness = PAPER_RULE_THICKNESS.dp, color = HhPaperColors.Rule)
    }
}

private class PaperMetrics {
    val rowGap: Dp = PAPER_ROW_GAP.dp
    val headingTop: Dp = PAPER_HEADING_TOP.dp
    val headingBottom: Dp = PAPER_HEADING_BOTTOM.dp

    @Composable
    fun name(): TextStyle = style(size = PAPER_NAME_SIZE, weight = FontWeight.Bold)

    @Composable
    fun body(bold: Boolean): TextStyle =
        style(size = PAPER_BODY_SIZE, weight = if (bold) FontWeight.Bold else FontWeight.Normal)

    @Composable
    fun heading(): TextStyle =
        style(size = PAPER_HEADING_SIZE, weight = FontWeight.Bold).copy(letterSpacing = PAPER_HEADING_TRACKING.em)

    @Composable
    private fun style(size: Float, weight: FontWeight): TextStyle {
        val density = LocalDensity.current
        val fontSize = with(density) { size.dp.toSp() }
        val lineHeight = with(density) { (size * PAPER_LINE_RATIO).dp.toSp() }
        return HhTheme.typography.bodyM.copy(fontSize = fontSize, lineHeight = lineHeight, fontWeight = weight)
    }
}
