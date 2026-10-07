package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhProvenanceChip(
    kind: HhProvenanceKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = HhTheme.colors
    val content = colors.provenanceContent(kind)
    HhPill(
        container = colors.primaryContainer,
        content = content,
        height = HhHeightChip,
        modifier = modifier,
    ) {
        Icon(
            imageVector = kind.glyph(),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(HhSizeChipIcon),
        )
        Text(
            text = label ?: kind.defaultLabel(),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = content,
        )
    }
}

private fun HhColors.provenanceContent(kind: HhProvenanceKind): Color = when (kind) {
    HhProvenanceKind.Confirmed -> primary
    HhProvenanceKind.UserStated -> partial
    HhProvenanceKind.UserEdited -> onSurface
    HhProvenanceKind.Scanned -> onSurfaceVariant
}

private fun HhProvenanceKind.glyph(): ImageVector = when (this) {
    HhProvenanceKind.Confirmed -> HhIcons.Check
    HhProvenanceKind.UserStated -> HhIcons.Chat
    HhProvenanceKind.UserEdited -> HhIcons.Edit
    HhProvenanceKind.Scanned -> HhIcons.Scan
}

@Preview(showBackground = true)
@Composable
private fun HhProvenanceChipPreview() {
    HhPreviewTheme(darkTheme = false) { HhProvenanceChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhProvenanceChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhProvenanceChipPreviewColumn() }
}

@Composable
private fun HhProvenanceChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhProvenanceKind.entries.forEach { kind -> HhProvenanceChip(kind = kind) }
    }
}
