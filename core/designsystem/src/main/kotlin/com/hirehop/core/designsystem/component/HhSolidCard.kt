package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhAccent { Coral, Jade, Marigold }

internal fun HhColors.accentFill(accent: HhAccent): Color = when (accent) {
    HhAccent.Coral -> header
    HhAccent.Jade -> brand
    HhAccent.Marigold -> primaryContainer
}

internal fun HhColors.onAccent(accent: HhAccent): Color = when (accent) {
    HhAccent.Coral -> onHeader
    HhAccent.Jade -> onBrand
    HhAccent.Marigold -> onPrimaryContainer
}

enum class HhOnColorChipStyle { White, Outline, Ink }

@Composable
fun HhOnColorChip(
    label: String,
    modifier: Modifier = Modifier,
    style: HhOnColorChipStyle = HhOnColorChipStyle.White,
    accent: HhAccent = HhAccent.Coral,
) {
    val colors = HhTheme.colors
    val onAccent = colors.onAccent(accent)
    val container = when (style) {
        HhOnColorChipStyle.White -> Color.White
        HhOnColorChipStyle.Outline -> Color.Transparent
        HhOnColorChipStyle.Ink -> colors.onSpecial
    }
    val content = when (style) {
        HhOnColorChipStyle.White -> colors.onSpecial
        HhOnColorChipStyle.Outline -> onAccent
        HhOnColorChipStyle.Ink -> Color.White
    }
    HhPill(
        container = container,
        content = content,
        height = HhHeightChipOnColor,
        modifier = modifier,
        border = if (style == HhOnColorChipStyle.Outline) BorderStroke(HhWidthStroke, onAccent) else null,
        textStyle = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
    ) {
        Text(text = label, color = content)
    }
}

@Immutable
data class HhOpenAction(val contentDescription: String, val onClick: () -> Unit)

@Composable
fun HhSolidCard(
    accent: HhAccent,
    monogram: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    openAction: HhOpenAction? = null,
    decoration: HhDecorationKind? = null,
    chips: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    val fill = colors.accentFill(accent)
    val onFill = colors.onAccent(accent)
    val ink = colors.onSpecial
    Surface(modifier = modifier.fillMaxWidth(), shape = HhTheme.shapes.card, color = fill, contentColor = onFill) {
        Box {
            if (decoration != null) {
                HhDecoration(
                    kind = decoration,
                    color = onFill.copy(alpha = 0.25f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = 70.dp),
                )
            }
            Column(
                modifier = Modifier.padding(HhTheme.spacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.cardPadding),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HhAccentMonogram(monogram, ink)
                    if (openAction != null) {
                        HhIconButton(
                            icon = HhIcons.ArrowForward,
                            contentDescription = openAction.contentDescription,
                            onClick = openAction.onClick,
                            tint = Color.White,
                            containerColor = ink,
                            borderColor = Color.Transparent,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
                    Text(text = title, style = HhTheme.typography.headlineM, color = onFill)
                    Text(
                        text = subtitle,
                        style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                        color = onFill,
                    )
                }
                if (chips != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), content = chips)
                }
            }
        }
    }
}

@Composable
internal fun HhAccentMonogram(text: String, letterColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(HhSizeAccentMonogram)
            .background(Color.White, HhTheme.shapes.pill)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.trim().take(2).uppercase(),
            style = HhTheme.typography.titleM.copy(fontWeight = FontWeight.ExtraBold),
            color = letterColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhSolidCardPreview() {
    HhPreviewTheme(darkTheme = false) { HhSolidCardSamples() }
}

@Preview(showBackground = true)
@Composable
private fun HhSolidCardDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhSolidCardSamples() }
}

@Composable
private fun HhSolidCardSamples() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSolidCard(
            accent = HhAccent.Coral,
            monogram = "LH",
            title = "Data Analyst",
            subtitle = "Lumen Health · Pune",
            openAction = HhOpenAction("Open Data Analyst") {},
            chips = {
                HhOnColorChip("Resume ready")
                HhOnColorChip("11 met", style = HhOnColorChipStyle.Outline)
            },
        )
        HhSolidCard(
            accent = HhAccent.Marigold,
            monogram = "NB",
            title = "Operations Associate",
            subtitle = "Northbay Logistics · Hyderabad",
            chips = { HhOnColorChip("Draft", style = HhOnColorChipStyle.Ink) },
        )
    }
}
