package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = HhTheme.colors.onSurface,
    containerColor: Color = HhTheme.colors.surface,
    borderColor: Color = HhTheme.colors.outlineSoft,
    shape: Shape = HhTheme.shapes.pill,
    size: Dp = HhHeightTouch,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .size(size)
            .semantics(mergeDescendants = true) { this.contentDescription = contentDescription },
        shape = shape,
        color = containerColor,
        border = if (borderColor == Color.Transparent) null else BorderStroke(HhWidthStroke, borderColor),
    ) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(HhSizeIcon),
            )
        }
    }
}

@Composable
fun HhHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        tint = HhTheme.colors.onHeaderControl,
        containerColor = HhTheme.colors.headerControl,
        borderColor = Color.Transparent,
    )
}

@Composable
fun HhBackButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHeader: Boolean = false,
) {
    if (onHeader) {
        HhHeaderIconButton(icon = HhIcons.Back, contentDescription = contentDescription, onClick = onClick, modifier = modifier)
    } else {
        HhIconButton(
            icon = HhIcons.Back,
            contentDescription = contentDescription,
            onClick = onClick,
            modifier = modifier,
            containerColor = HhTheme.colors.card,
            borderColor = Color.Transparent,
        )
    }
}

private const val DISABLED_ALPHA = 0.38f

@Preview(showBackground = true)
@Composable
private fun HhIconButtonPreview() {
    HhPreviewTheme(darkTheme = false) { HhIconButtonPreviewRow() }
}

@Preview(showBackground = true)
@Composable
private fun HhIconButtonDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhIconButtonPreviewRow() }
}

@Composable
private fun HhIconButtonPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhIconButton(icon = HhIcons.Add, contentDescription = "Add", onClick = {})
        HhIconButton(icon = HhIcons.Delete, contentDescription = "Delete", onClick = {}, enabled = false)
        HhIconButton(
            icon = HhIcons.Share,
            contentDescription = "Share",
            onClick = {},
            containerColor = HhTheme.colors.primaryContainer,
            tint = HhTheme.colors.onPrimaryContainer,
            borderColor = Color.Transparent,
        )
    }
}
