package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

enum class TmrPillRowStyle { Coral, Jade, Marigold, Ink, Neutral }

private class TmrPillRowPalette(
    val container: Color,
    val content: Color,
    val subtitle: Color,
    val circle: Color,
    val onCircle: Color,
    val border: BorderStroke?,
)

@Composable
private fun pillRowPalette(style: TmrPillRowStyle): TmrPillRowPalette {
    val colors = TmrTheme.colors
    return when (style) {
        TmrPillRowStyle.Coral -> TmrPillRowPalette(colors.header, colors.onHeader, colors.onHeaderVariant, colors.brand, colors.onBrand, null)
        TmrPillRowStyle.Jade -> TmrPillRowPalette(colors.brand, colors.onBrand, colors.onBrand, colors.header, colors.brand, null)
        TmrPillRowStyle.Marigold ->
            TmrPillRowPalette(colors.primaryContainer, colors.onPrimaryContainer, colors.onPrimaryContainer, colors.brand, colors.onBrand, null)
        TmrPillRowStyle.Ink -> TmrPillRowPalette(
            colors.inverseSurface,
            colors.inverseOnSurface,
            colors.inverseOnSurface.copy(alpha = INK_SUBTITLE_ALPHA),
            colors.inverseOnSurface,
            colors.inverseSurface,
            null,
        )
        TmrPillRowStyle.Neutral -> TmrPillRowPalette(
            colors.card,
            colors.onSurface,
            colors.onSurfaceVariant,
            colors.brand,
            colors.onBrand,
            BorderStroke(TmrWidthHairline, colors.outlineVariant),
        )
    }
}

@Composable
fun TmrPillRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TmrPillRowStyle = TmrPillRowStyle.Coral,
    subtitle: String? = null,
    icon: ImageVector? = null,
    monogram: String? = null,
    trailingIcon: ImageVector? = TmrIcons.ArrowForward,
    leading: (@Composable () -> Unit)? = null,
    titleMaxLines: Int = Int.MAX_VALUE,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val palette = pillRowPalette(style)
    val quietDisc = style == TmrPillRowStyle.Neutral && leading == null && icon == null && monogram != null
    val circle = if (quietDisc) TmrTheme.colors.primaryContainer else palette.circle
    val onCircle = if (quietDisc) TmrTheme.colors.onSurface else palette.onCircle
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().tmrPressScale(source),
        shape = TmrTheme.shapes.pillRow,
        color = palette.container,
        contentColor = palette.content,
        border = palette.border,
        interactionSource = source,
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = TmrHeightPillRow).padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(TmrHeightTouch)
                    .background(circle, TmrTheme.shapes.pill)
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                if (leading != null) {
                    leading()
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = palette.onCircle, modifier = Modifier.size(22.dp))
                } else if (monogram != null) {
                    Text(
                        text = monogram.trim().take(2).uppercase(),
                        style = TmrTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold),
                        color = onCircle,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TmrTheme.typography.titleM,
                    color = palette.content,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = TmrTheme.typography.labelM,
                        color = palette.subtitle,
                    )
                }
                if (supporting != null) {
                    Column(
                        modifier = Modifier.padding(top = TmrTheme.spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                        content = supporting,
                    )
                }
            }
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = palette.content, modifier = Modifier.size(22.dp))
            }
        }
    }
}

private const val INK_SUBTITLE_ALPHA = 0.75f

@Preview(showBackground = true)
@Composable
private fun TmrPillRowPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrPillRowSamples() }
}

@Preview(showBackground = true)
@Composable
private fun TmrPillRowDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrPillRowSamples() }
}

@Composable
private fun TmrPillRowSamples() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrPillRow("Choose a PDF", {}, style = TmrPillRowStyle.Coral, subtitle = "Text PDFs read best", icon = TmrIcons.Description)
        TmrPillRow("Choose a DOCX", {}, style = TmrPillRowStyle.Jade, subtitle = "Word documents", icon = TmrIcons.Description)
        TmrPillRow("Start without a resume", {}, style = TmrPillRowStyle.Marigold, icon = TmrIcons.Add)
        TmrPillRow("Your data and consent", {}, style = TmrPillRowStyle.Ink, icon = TmrIcons.Lock)
        TmrPillRow("Product Analyst", {}, style = TmrPillRowStyle.Neutral, subtitle = "Kestrel Pay · Bengaluru", monogram = "KP")
    }
}
