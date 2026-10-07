package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhButtonSize { Large, Compact }

enum class HhButtonKind { Primary, Secondary, Outline, Text, Destructive, Ink }

object HhButtonDefaults {
    val ContentPadding = PaddingValues(horizontal = 22.dp)
    val TextContentPadding = PaddingValues(horizontal = 12.dp)
    val IconSize = 20.dp
    val CompactContentPadding = PaddingValues(horizontal = 16.dp)
    val CompactIconSize = 17.dp
}

private class HhButtonPalette(val container: Color, val content: Color, val border: BorderStroke?)

private fun HhColors.buttonPalette(
    kind: HhButtonKind,
    surface: HhButtonSurface,
    pressed: Boolean,
): HhButtonPalette = when (kind) {
    HhButtonKind.Primary -> HhButtonPalette(if (pressed) brandPressed else brand, onBrand, null)
    HhButtonKind.Ink -> HhButtonPalette(inverseSurface, inverseOnSurface, null)
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
    HhButtonSurface.Default -> HhButtonPalette(
        Color.Transparent,
        onSurface,
        BorderStroke(HhWidthStroke, outlineVariant),
    )
}

private fun HhColors.disabledPalette(): HhButtonPalette = HhButtonPalette(primaryContainer, onSurfaceVariant, null)

private fun HhButtonKind.isFilled(): Boolean = this != HhButtonKind.Outline && this != HhButtonKind.Text

private fun HhColors.textPalette(surface: HhButtonSurface): HhButtonPalette = when (surface) {
    HhButtonSurface.Header -> HhButtonPalette(Color.Transparent, onHeader, null)
    HhButtonSurface.Default -> HhButtonPalette(Color.Transparent, primary, null)
}

@Composable
internal fun HhButtonBase(
    kind: HhButtonKind,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    contentPadding: PaddingValues,
    size: HhButtonSize = HhButtonSize.Large,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val minHeight = if (size == HhButtonSize.Compact) HhHeightButtonCompact else HhHeightButtonLarge
    val filledDisabled = !enabled && kind.isFilled()
    val palette = if (filledDisabled) {
        HhTheme.colors.disabledPalette()
    } else {
        HhTheme.colors.buttonPalette(kind, LocalHhButtonSurface.current, pressed)
    }
    val focused by source.collectIsFocusedAsState()
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .hhPressScale(source)
            .hhFocusRing(focused, HhTheme.colors.primary, 24.dp)
            .alpha(if (enabled || filledDisabled) 1f else DISABLED_ALPHA)
            .defaultMinSize(minHeight = minHeight),
        shape = HhTheme.shapes.pill,
        color = palette.container,
        contentColor = palette.content,
        border = palette.border,
        interactionSource = source,
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.content) {
            ProvideTextStyle(HhTheme.typography.button) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = minHeight)
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
    size: HhButtonSize = HhButtonSize.Large,
) {
    HhButtonBase(
        kind = kind,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        contentPadding = when {
            kind == HhButtonKind.Text -> HhButtonDefaults.TextContentPadding
            size == HhButtonSize.Compact -> HhButtonDefaults.CompactContentPadding
            else -> HhButtonDefaults.ContentPadding
        },
    ) {
        HhButtonIcon(leadingIcon, size)
        var wrapped by remember { mutableStateOf(false) }
        Text(
            text = label,
            style = if (size == HhButtonSize.Compact) HhTheme.typography.labelL else HhTheme.typography.button,
            textAlign = if (wrapped) TextAlign.Center else TextAlign.Unspecified,
            onTextLayout = { wrapped = it.lineCount > 1 },
        )
        HhButtonIcon(trailingIcon, size)
    }
}

@Composable
private fun HhButtonIcon(icon: ImageVector?, size: HhButtonSize) {
    if (icon != null) {
        val iconSize = if (size == HhButtonSize.Compact) HhButtonDefaults.CompactIconSize else HhButtonDefaults.IconSize
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(iconSize))
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
    HhButtonBase(HhButtonKind.Primary, onClick, modifier, enabled, contentPadding, content = content)
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
    HhButtonBase(HhButtonKind.Outline, onClick, modifier, enabled, contentPadding, content = content)
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
    size: HhButtonSize = HhButtonSize.Large,
) {
    HhButtonLabeled(HhButtonKind.Primary, label, onClick, modifier, enabled, leadingIcon, trailingIcon, size)
}

@Composable
fun HhSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    size: HhButtonSize = HhButtonSize.Large,
) {
    HhButtonLabeled(HhButtonKind.Secondary, label, onClick, modifier, enabled, leadingIcon, trailingIcon, size)
}

@Composable
fun HhOutlineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    size: HhButtonSize = HhButtonSize.Large,
) {
    HhButtonLabeled(HhButtonKind.Outline, label, onClick, modifier, enabled, leadingIcon, trailingIcon, size)
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
fun HhInkButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    HhButtonLabeled(HhButtonKind.Ink, label, onClick, modifier, enabled, leadingIcon, trailingIcon)
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

private const val DISABLED_ALPHA = 0.38f
