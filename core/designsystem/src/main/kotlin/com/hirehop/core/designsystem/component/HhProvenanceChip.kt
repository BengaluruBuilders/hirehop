package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
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
fun HhProvenanceChip(
    kind: HhProvenanceKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = HhTheme.colors
    val contentColor = if (kind == HhProvenanceKind.Confirmed) {
        colors.success
    } else {
        colors.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(HhTheme.shapes.full),
        color = colors.surfaceContainerLow,
        border = BorderStroke(width = HhWidthHairline, color = colors.hairlineStrong),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = HhTheme.spacing.xs,
                vertical = HhTheme.spacing.xxs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            Icon(
                imageVector = kind.glyph(),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(HhSizeIcon),
            )
            Text(
                text = label ?: kind.defaultLabel(),
                style = HhTheme.typography.labelMedium,
                color = contentColor,
            )
        }
    }
}

private fun HhProvenanceKind.glyph(): ImageVector =
    when (this) {
        HhProvenanceKind.Confirmed -> Icons.Rounded.Check
        HhProvenanceKind.UserStated -> Icons.Rounded.Person
        HhProvenanceKind.UserEdited -> Icons.Rounded.Edit
        HhProvenanceKind.Scanned -> Icons.Rounded.DocumentScanner
    }

private val HhProvenanceChipAllKinds: List<HhProvenanceKind> = HhProvenanceKind.entries

@Preview(showBackground = true)
@Composable
private fun HhProvenanceChipPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhProvenanceChipPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhProvenanceChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhProvenanceChipPreviewRow()
    }
}

@Composable
private fun HhProvenanceChipPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhProvenanceChipAllKinds.forEach { kind ->
            HhProvenanceChip(kind = kind)
        }
    }
}
