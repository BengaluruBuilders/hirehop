package com.hirehop.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow

@Composable
fun HhDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                horizontal = HhTheme.spacing.gutter,
                vertical = HhTheme.spacing.sm,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HhDockHeight)
                .hhShadow(HhTheme.elevation.dock, HhDockShape)
                .clip(HhDockShape)
                .background(HhTheme.colors.tool)
                .then(
                    if (HhTheme.isDark) {
                        Modifier.border(HhWidthHairline, HhTheme.colors.outlineSoft, HhDockShape)
                    } else {
                        Modifier
                    },
                )
                .padding(HhDockPadding),
            horizontalArrangement = Arrangement.spacedBy(HhDockGap),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun RowScope.HhDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val iconOnly = LocalDensity.current.fontScale >= 1.5f
    val colors = HhTheme.colors
    val tint by animateColorAsState(
        targetValue = if (selected) colors.onBrand else colors.onToolVariant,
        animationSpec = HhTheme.motion.proofSpecs.color,
        label = "dockTint",
    )
    val fill by animateColorAsState(
        targetValue = if (selected) colors.brand else Color.Transparent,
        animationSpec = HhTheme.motion.proofSpecs.color,
        label = "dockFill",
    )
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .hhPressScale(interactionSource)
            .clip(HhDockItemShape)
            .background(fill)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics { if (iconOnly) contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhDockItemGap, Alignment.CenterVertically),
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) { icon() }
        if (!iconOnly) {
            Text(
                text = label,
                style = HhTheme.typography.labelM.copy(
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                ),
                color = tint,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

private val HhDockHeight = 68.dp
private val HhDockPadding = 6.dp
private val HhDockGap = 4.dp
private val HhDockItemGap = 3.dp
private val HhDockShape = RoundedCornerShape(34.dp)
private val HhDockItemShape = RoundedCornerShape(28.dp)

private val HhDockIconSize = 22.dp

@Composable
fun HhDockIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(HhDockIconSize))
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
    HhDock {
        HhDockItem(
            selected = true,
            onClick = {},
            label = "Applications",
            icon = { HhDockIcon(HhIcons.Applications) },
        )
        HhDockItem(
            selected = false,
            onClick = {},
            label = "Profile",
            icon = { HhDockIcon(HhIcons.Profile) },
        )
        HhDockItem(
            selected = false,
            onClick = {},
            label = "Settings",
            icon = { HhDockIcon(HhIcons.Settings) },
        )
    }
}
