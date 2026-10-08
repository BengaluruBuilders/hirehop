package com.tailormyresume.core.designsystem.component

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
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrColors
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrProvenanceChip(
    kind: TmrProvenanceKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = TmrTheme.colors
    val content = colors.provenanceContent(kind)
    TmrPill(
        container = colors.primaryContainer,
        content = content,
        height = TmrHeightChip,
        modifier = modifier,
    ) {
        Icon(
            imageVector = kind.glyph(),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(TmrSizeChipIcon),
        )
        Text(
            text = label ?: kind.defaultLabel(),
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = content,
        )
    }
}

private fun TmrColors.provenanceContent(kind: TmrProvenanceKind): Color = when (kind) {
    TmrProvenanceKind.Confirmed -> primary
    TmrProvenanceKind.UserStated -> partial
    TmrProvenanceKind.UserEdited -> onSurface
    TmrProvenanceKind.Scanned -> onSurfaceVariant
}

private fun TmrProvenanceKind.glyph(): ImageVector = when (this) {
    TmrProvenanceKind.Confirmed -> TmrIcons.Check
    TmrProvenanceKind.UserStated -> TmrIcons.Chat
    TmrProvenanceKind.UserEdited -> TmrIcons.Edit
    TmrProvenanceKind.Scanned -> TmrIcons.Scan
}

@Preview(showBackground = true)
@Composable
private fun TmrProvenanceChipPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrProvenanceChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun TmrProvenanceChipDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrProvenanceChipPreviewColumn() }
}

@Composable
private fun TmrProvenanceChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrProvenanceKind.entries.forEach { kind -> TmrProvenanceChip(kind = kind) }
    }
}
