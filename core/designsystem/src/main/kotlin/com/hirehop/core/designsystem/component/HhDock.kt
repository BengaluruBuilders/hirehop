package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow

@Composable
fun HhDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val shape = HhTheme.shapes.pill
    Surface(
        modifier = modifier
            .widthIn(max = HhMaxWidthDock)
            .fillMaxWidth()
            .hhShadow(HhTheme.elevation.dock, shape),
        shape = shape,
        color = colors.tool,
        contentColor = colors.onTool,
        border = if (HhTheme.isDark) BorderStroke(HhWidthHairline, colors.outlineSoft) else null,
    ) {
        Row(
            modifier = Modifier.height(HhHeightDock).padding(horizontal = HhTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun HhDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: (@Composable () -> Unit)? = null,
) {
    val colors = HhTheme.colors
    val tint = if (selected) colors.onPrimary else colors.onToolVariant
    val background = if (selected) colors.primary else androidx.compose.ui.graphics.Color.Transparent
    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = HhHeightTouch, minHeight = HhHeightTouch)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        shape = HhTheme.shapes.pill,
        color = background,
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) {
            ProvideTextStyle(HhTheme.typography.labelL) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minWidth = HhHeightTouch, minHeight = HhHeightTouch)
                        .padding(horizontal = if (selected && label != null) 18.dp else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(HhSizeIcon + 2.dp), contentAlignment = Alignment.Center) { icon() }
                    if (selected && label != null) {
                        label()
                    }
                }
            }
        }
    }
}

@Composable
fun HhDockIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(HhSizeIcon + 2.dp))
}

@Preview(showBackground = true)
@Composable
private fun HhDockPreview() {
    HhPreviewTheme(darkTheme = false) { HhDockSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhDockDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhDockSample() }
}

@Composable
private fun HhDockSample() {
    HhDock(modifier = Modifier.padding(HhTheme.spacing.gutter)) {
        HhDockItem(
            selected = true,
            onClick = {},
            contentDescription = "Home",
            icon = { HhDockIcon(HhIcons.Home) },
            label = { Text("Home") },
        )
        HhDockItem(selected = false, onClick = {}, contentDescription = "Applications", icon = { HhDockIcon(HhIcons.Applications) })
        HhDockItem(selected = false, onClick = {}, contentDescription = "Facts", icon = { HhDockIcon(HhIcons.Facts) })
        HhDockItem(selected = false, onClick = {}, contentDescription = "Profile", icon = { HhDockIcon(HhIcons.Profile) })
    }
}
