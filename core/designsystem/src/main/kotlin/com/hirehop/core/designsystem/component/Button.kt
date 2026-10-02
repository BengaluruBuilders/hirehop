package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhButtonKind { Primary, Secondary, Outline, Text, Destructive }

object HhButtonDefaults {
    val ContentPadding = PaddingValues(horizontal = 22.dp)
    val TextContentPadding = PaddingValues(horizontal = 12.dp)
    val IconSize = 18.dp
}

private class HhButtonPalette(val container: Color, val content: Color, val border: BorderStroke?)

private fun HhColors.buttonPalette(
    kind: HhButtonKind,
    surface: HhButtonSurface,
): HhButtonPalette = when (kind) {
    HhButtonKind.Primary -> HhButtonPalette(primary, onPrimary, null)
    HhButtonKind.Secondary -> HhButtonPalette(primaryContainer, onPrimaryContainer, null)
    HhButtonKind.Destructive -> HhButtonPalette(error, onError, null)
    HhButtonKind.Outline -> outlinePalette(surface)
    HhButtonKind.Text -> textPalette(surface)
}

private fun HhColors.outlinePalette(surface: HhButtonSurface): HhButtonPalette = when (surface) {
    HhButtonSurface.Header -> HhButtonPalette(
        Color.Transparent,
        onHeader,
        BorderStroke(HhWidthStroke, onHeader.copy(alpha = 0.85f)),
    )
    HhButtonSurface.Tool -> HhButtonPalette(
        Color.Transparent,
        onTool,
        BorderStroke(HhWidthStroke, onTool.copy(alpha = 0.35f)),
    )
    HhButtonSurface.Default -> HhButtonPalette(
        Color.Transparent,
        onSurface,
        BorderStroke(HhWidthStroke, outlineSoft),
    )
}

private fun HhColors.textPalette(surface: HhButtonSurface): HhButtonPalette = when (surface) {
    HhButtonSurface.Header -> HhButtonPalette(Color.Transparent, onHeader, null)
    HhButtonSurface.Tool -> HhButtonPalette(Color.Transparent, inversePrimary, null)
    HhButtonSurface.Default -> HhButtonPalette(Color.Transparent, primary, null)
}

@Composable
internal fun HhButtonBase(
    kind: HhButtonKind,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    contentPadding: PaddingValues,
    content: @Composable RowScope.() -> Unit,
) {
    val palette = HhTheme.colors.buttonPalette(kind, LocalHhButtonSurface.current)
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .hhFocusRing(focused, HhTheme.colors.primary, 24.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .defaultMinSize(minHeight = HhHeightTouch),
        shape = HhTheme.shapes.pill,
        color = palette.container,
        contentColor = palette.content,
        border = palette.border,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.content) {
            ProvideTextStyle(HhTheme.typography.labelL) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = HhHeightTouch)
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }
        }
    }
}

@Composable
internal fun HhButtonLabeled(
    kind: HhButtonKind,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
) {
    HhButtonBase(
        kind = kind,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = if (kind == HhButtonKind.Text) {
            HhButtonDefaults.TextContentPadding
        } else {
            HhButtonDefaults.ContentPadding
        },
    ) {
        HhButtonIcon(leadingIcon)
        Text(text = label, style = HhTheme.typography.labelL)
        HhButtonIcon(trailingIcon)
    }
}

@Composable
private fun HhButtonIcon(icon: ImageVector?) {
    if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(HhButtonDefaults.IconSize))
    }
}

@Composable
private fun HhButtonSlots(
    kind: HhButtonKind,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)?,
) {
    HhButtonBase(
        kind = kind,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = HhButtonDefaults.ContentPadding,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        text()
    }
}

@Composable
fun HhButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = HhButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    HhButtonBase(HhButtonKind.Primary, onClick, modifier, enabled, contentPadding, content)
}

@Composable
fun HhButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    HhButtonSlots(HhButtonKind.Primary, onClick, modifier, enabled, text, leadingIcon)
}

@Composable
fun HhOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = HhButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    HhButtonBase(HhButtonKind.Outline, onClick, modifier, enabled, contentPadding, content)
}

@Composable
fun HhOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    HhButtonSlots(HhButtonKind.Outline, onClick, modifier, enabled, text, leadingIcon)
}

@Composable
fun HhPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    HhButtonLabeled(HhButtonKind.Primary, label, onClick, modifier, enabled, leadingIcon, trailingIcon)
}

@Composable
fun HhSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    HhButtonLabeled(HhButtonKind.Secondary, label, onClick, modifier, enabled, leadingIcon, trailingIcon)
}

@Composable
fun HhOutlineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    HhButtonLabeled(HhButtonKind.Outline, label, onClick, modifier, enabled, leadingIcon, trailingIcon)
}

@Composable
fun HhTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    HhButtonLabeled(HhButtonKind.Text, label, onClick, modifier, enabled, leadingIcon, trailingIcon)
}

@Composable
fun HhDestructiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    HhButtonLabeled(HhButtonKind.Destructive, label, onClick, modifier, enabled, leadingIcon, null)
}

@Composable
fun HhHeaderButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
) {
    CompositionLocalProvider(LocalHhButtonSurface provides HhButtonSurface.Header) {
        HhButtonLabeled(HhButtonKind.Outline, label, onClick, modifier, true, null, trailingIcon)
    }
}

private const val DISABLED_ALPHA = 0.38f
