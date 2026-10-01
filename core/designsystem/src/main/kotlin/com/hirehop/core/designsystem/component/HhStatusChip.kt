package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhStatusChip(
    kind: HhStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = HhTheme.colors
    val container = when (kind) {
        HhStatusKind.Met -> colors.successContainer
        HhStatusKind.Partial -> colors.warningContainer
        HhStatusKind.Gap -> colors.gapContainer
    }
    val content = when (kind) {
        HhStatusKind.Met -> colors.onSuccessContainer
        HhStatusKind.Partial -> colors.onWarningContainer
        HhStatusKind.Gap -> colors.onGapContainer
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(HhTheme.shapes.full),
        color = container,
        border = BorderStroke(width = HhWidthHairline, color = colors.hairline),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = HhTheme.spacing.sm,
                vertical = HhTheme.spacing.xxs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            Icon(
                imageVector = kind.glyph(),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(HhSizeIcon),
            )
            Text(
                text = label ?: kind.defaultLabel(),
                style = HhTheme.typography.labelMedium,
                color = content,
            )
        }
    }
}

private fun HhStatusKind.glyph(): ImageVector =
    when (this) {
        HhStatusKind.Met -> Icons.Rounded.Check
        HhStatusKind.Partial -> Icons.Rounded.HourglassEmpty
        HhStatusKind.Gap -> Icons.Rounded.Add
    }

private val HhStatusChipAllKinds: List<HhStatusKind> = HhStatusKind.entries

@Preview(showBackground = true)
@Composable
private fun HhStatusChipPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhStatusChipPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhStatusChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhStatusChipPreviewRow()
    }
}

@Composable
private fun HhStatusChipPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhStatusChipAllKinds.forEach { kind ->
            HhStatusChip(kind = kind)
        }
    }
}
