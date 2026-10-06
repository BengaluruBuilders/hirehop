package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhPillRowStyle { Coral, Jade, Marigold, Ink, Neutral }

private class HhPillRowPalette(
    val container: Color,
    val content: Color,
    val subtitle: Color,
    val circle: Color,
    val onCircle: Color,
    val border: BorderStroke?,
)

@Composable
private fun pillRowPalette(style: HhPillRowStyle): HhPillRowPalette {
    val colors = HhTheme.colors
    return when (style) {
        HhPillRowStyle.Coral -> HhPillRowPalette(colors.coral, colors.onCoral, colors.onCoral, Color.White, colors.coral, null)
        HhPillRowStyle.Jade -> HhPillRowPalette(colors.brand, colors.onBrand, colors.onBrand, Color.White, colors.brand, null)
        HhPillRowStyle.Marigold ->
            HhPillRowPalette(colors.special, colors.onSpecial, colors.onSpecial, Color.White, colors.onSpecial, null)
        HhPillRowStyle.Ink -> HhPillRowPalette(
            colors.inverseSurface,
            colors.inverseOnSurface,
            colors.inverseOnSurface.copy(alpha = INK_SUBTITLE_ALPHA),
            colors.inverseOnSurface,
            colors.inverseSurface,
            null,
        )
        HhPillRowStyle.Neutral -> HhPillRowPalette(
            colors.card,
            colors.onSurface,
            colors.onSurfaceVariant,
            colors.brand,
            colors.onBrand,
            BorderStroke(HhWidthHairline, colors.outlineVariant),
        )
    }
}

@Composable
fun HhPillRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: HhPillRowStyle = HhPillRowStyle.Coral,
    subtitle: String? = null,
    icon: ImageVector? = null,
    monogram: String? = null,
    trailingIcon: ImageVector? = HhIcons.ArrowForward,
) {
    val palette = pillRowPalette(style)
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().hhPressScale(source),
        shape = HhTheme.shapes.pillRow,
        color = palette.container,
        contentColor = palette.content,
        border = palette.border,
        interactionSource = source,
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = HhHeightPillRow).padding(horizontal = HhTheme.spacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(HhHeightTouch)
                    .background(palette.circle, HhTheme.shapes.pill)
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = palette.onCircle, modifier = Modifier.size(22.dp))
                } else if (monogram != null) {
                    Text(
                        text = monogram.trim().take(2).uppercase(),
                        style = HhTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.onCircle,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = HhTheme.typography.titleM, color = palette.content)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = HhTheme.typography.labelM,
                        color = palette.subtitle,
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
private fun HhPillRowPreview() {
    HhPreviewTheme(darkTheme = false) { HhPillRowSamples() }
}

@Preview(showBackground = true)
@Composable
private fun HhPillRowDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhPillRowSamples() }
}

@Composable
private fun HhPillRowSamples() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhPillRow("Choose a PDF", {}, style = HhPillRowStyle.Coral, subtitle = "Text PDFs read best", icon = HhIcons.Description)
        HhPillRow("Choose a DOCX", {}, style = HhPillRowStyle.Jade, subtitle = "Word documents", icon = HhIcons.Description)
        HhPillRow("Start without a resume", {}, style = HhPillRowStyle.Marigold, icon = HhIcons.Add)
        HhPillRow("Your data and consent", {}, style = HhPillRowStyle.Ink, icon = HhIcons.Lock)
        HhPillRow("Product Analyst", {}, style = HhPillRowStyle.Neutral, subtitle = "Kestrel Pay · Bengaluru", monogram = "KP")
    }
}
