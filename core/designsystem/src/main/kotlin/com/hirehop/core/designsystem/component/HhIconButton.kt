package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

private const val HH_ICON_BUTTON_DISABLED_CONTENT_ALPHA = 0.38f
private const val HH_ICON_BUTTON_DISABLED_BORDER_ALPHA = 0.12f

@Composable
fun HhIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = HhTheme.colors.onSurface,
    containerColor: Color = HhTheme.colors.surface,
    borderColor: Color = HhTheme.colors.hairlineStrong,
    shape: Shape = RoundedCornerShape(HhTheme.shapes.sm),
) {
    val contentColor = if (enabled) {
        tint
    } else {
        tint.copy(alpha = HH_ICON_BUTTON_DISABLED_CONTENT_ALPHA)
    }
    val border = if (enabled) {
        borderColor
    } else {
        borderColor.copy(alpha = HH_ICON_BUTTON_DISABLED_BORDER_ALPHA)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .sizeIn(minWidth = HhTheme.spacing.d48, minHeight = HhTheme.spacing.d48)
            .semantics(mergeDescendants = true) { this.contentDescription = contentDescription },
        shape = shape,
        color = containerColor,
        border = BorderStroke(width = HhWidthHairline, color = border),
    ) {
        Box(
            modifier = Modifier.size(HhSizeIcon),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HhIconButtonPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhIconButtonPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhIconButtonDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhIconButtonPreviewRow()
    }
}

@Composable
private fun HhIconButtonPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhIconButton(
            icon = HhIcons.Add,
            contentDescription = "Add",
            onClick = {},
        )
        HhIconButton(
            icon = HhIcons.Delete,
            contentDescription = "Delete",
            onClick = {},
            enabled = false,
        )
        HhIconButton(
            icon = HhIcons.Share,
            contentDescription = "Share",
            onClick = {},
            containerColor = HhTheme.colors.primaryContainer,
            tint = HhTheme.colors.onPrimaryContainer,
            borderColor = HhTheme.colors.primary,
        )
    }
}
