package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.VerifiedUser
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
fun HhTrustChip(
    kind: HhTrustKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(HhTheme.shapes.full),
        color = HhTheme.colors.surface,
        border = BorderStroke(width = HhWidthHairline, color = HhTheme.colors.hairline),
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
                tint = HhTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(HhSizeIcon),
            )
            Text(
                text = label ?: kind.defaultLabel(),
                style = HhTheme.typography.labelSmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

private fun HhTrustKind.glyph(): ImageVector =
    when (this) {
        HhTrustKind.NeverInvents -> Icons.Rounded.VerifiedUser
        HhTrustKind.OnDevice -> Icons.Rounded.PhoneAndroid
        HhTrustKind.OfflineReady -> Icons.Rounded.CloudOff
        HhTrustKind.NoScore -> Icons.Rounded.Remove
    }

private val HhTrustChipAllKinds: List<HhTrustKind> = HhTrustKind.entries

@Preview(showBackground = true)
@Composable
private fun HhTrustChipPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhTrustChipPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhTrustChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhTrustChipPreviewRow()
    }
}

@Composable
private fun HhTrustChipPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhTrustChipAllKinds.forEach { kind ->
            HhTrustChip(kind = kind)
        }
    }
}
