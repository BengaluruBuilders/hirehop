package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhTrustChip(
    kind: HhTrustKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = HhTheme.colors
    HhPill(
        container = colors.primaryContainer,
        content = colors.onPrimaryContainer,
        height = HhHeightChipLarge,
        modifier = modifier,
    ) {
        Icon(
            imageVector = kind.glyph(),
            contentDescription = null,
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label ?: kind.defaultLabel(),
            style = HhTheme.typography.labelM,
            color = colors.onPrimaryContainer,
        )
    }
}

private fun HhTrustKind.glyph(): ImageVector = when (this) {
    HhTrustKind.NeverInvents -> HhIcons.CheckCircle
    HhTrustKind.OnDevice -> HhIcons.Phone
    HhTrustKind.OfflineReady -> HhIcons.OfflineCloud
    HhTrustKind.NoScore -> HhIcons.Block
}

@Preview(showBackground = true)
@Composable
private fun HhTrustChipPreview() {
    HhPreviewTheme(darkTheme = false) { HhTrustChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhTrustChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhTrustChipPreviewColumn() }
}

@Composable
private fun HhTrustChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhTrustKind.entries.forEach { kind -> HhTrustChip(kind = kind) }
    }
}
