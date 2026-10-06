package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhElevationDefaults
import com.hirehop.core.designsystem.theme.HhLightColors
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.LocalHhColors
import com.hirehop.core.designsystem.theme.LocalHhDark
import com.hirehop.core.designsystem.theme.LocalHhElevation
import com.hirehop.core.designsystem.theme.hhShadow

@Composable
fun HhFitShareCard(
    eyebrow: String,
    headline: String,
    met: Int,
    partial: Int?,
    gap: Int,
    caption: String,
    metLegend: String,
    partialLegend: String?,
    gapLegend: String,
    matchedTerms: List<String>,
    footer: String,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalHhColors provides HhLightColors,
        LocalHhDark provides false,
        LocalHhElevation provides HhElevationDefaults.Light,
    ) {
        val colors = HhTheme.colors
        val shape = HhTheme.shapes.heroCard
        Surface(
            modifier = modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .hhShadow(HhTheme.elevation.hero, shape),
            shape = shape,
            color = colors.document,
            border = BorderStroke(HhWidthHairline, colors.outlineVariant),
        ) {
            Column {
                HhFitShareHeader(eyebrow, headline)
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                ) {
                    HhCoverageBlock(met, partial, gap, caption, metLegend, partialLegend, gapLegend)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        matchedTerms.forEach { HhTermChip(label = it) }
                    }
                    Text(text = footer, style = HhTheme.typography.labelM, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun HhFitShareHeader(eyebrow: String, headline: String) {
    val colors = HhTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clipToBounds()
            .drawBehind {
                drawRect(colors.header)
                drawDecoration(
                    HhDecorationKind.Ring,
                    colors.special,
                    Offset(size.width - 56.dp.toPx(), 20.dp.toPx()),
                    34.dp.toPx(),
                    2.6.dp.toPx(),
                )
            }
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            Text(text = eyebrow, style = HhTheme.typography.labelM, color = colors.onHeaderVariant)
            Text(text = headline, style = HhTheme.typography.titleL, color = colors.onHeader)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HhFitShareCardPreview() {
    HhPreviewTheme(darkTheme = false) { HhFitShareCardSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhFitShareCardDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhFitShareCardSample() }
}

@Composable
private fun HhFitShareCardSample() {
    HhFitShareCard(
        eyebrow = "JD fit · Android Developer · Kestrel Labs",
        headline = "Priya's fit, from confirmed facts",
        met = 9,
        partial = 2,
        gap = 3,
        caption = "key terms covered",
        metLegend = "9 Met",
        partialLegend = "2 Partly met",
        gapLegend = "3 To prepare",
        matchedTerms = listOf("Kotlin", "Jetpack Compose", "Git"),
        footer = "HireHop · We never invent anything about you.",
        modifier = Modifier.padding(HhTheme.spacing.gutter),
    )
}
