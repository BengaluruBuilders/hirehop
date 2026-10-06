package com.hirehop.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Immutable
data class HhFilterChipColors(
    val selectedContainer: Color,
    val onSelectedContainer: Color,
    val container: Color,
    val onContainer: Color,
    val border: Color,
    val selectedBorder: Color,
)

@Composable
fun HhFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    count: Int? = null,
    colors: HhFilterChipColors = HhFilterChipDefaults.colors(),
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.selectedContainer else colors.container,
        animationSpec = HhTheme.motion.proofSpecs.color,
        label = "hhFilterChipContainer",
    )
    val contentColor = if (selected) colors.onSelectedContainer else colors.onContainer
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = HhFilterChipDefaults.height())
            .selectable(
                selected = selected,
                enabled = true,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        shape = HhTheme.shapes.pill,
        color = containerColor,
        border = BorderStroke(
            width = if (selected) HhWidthStroke else HhWidthHairline,
            color = if (selected) colors.selectedBorder else colors.border,
        ),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = HhFilterChipDefaults.SelectedContainerHeight)
                .padding(
                    horizontal = HhSpacingFourteen,
                    vertical = HhTheme.spacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            val glyph = if (selected) HhFilterChipDefaults.selectedTick() else leadingIcon
            if (glyph != null) {
                Icon(
                    imageVector = glyph,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(HhSizeChipIcon),
                )
            }
            Text(
                text = label,
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
                maxLines = 1,
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    style = HhTheme.typography.labelL,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }
    }
}

object HhFilterChipDefaults {
    val SelectedContainerHeight: Dp
        @Composable
        @ReadOnlyComposable
        get() = HhTheme.spacing.xs

    @Composable
    fun colors(
        selectedContainer: Color = HhTheme.colors.inverseSurface,
        onSelectedContainer: Color = HhTheme.colors.inverseOnSurface,
        container: Color = HhTheme.colors.card,
        onContainer: Color = HhTheme.colors.onSurface,
        border: Color = HhTheme.colors.outlineVariant,
        selectedBorder: Color = HhTheme.colors.inverseSurface,
    ): HhFilterChipColors = HhFilterChipColors(
        selectedContainer = selectedContainer,
        onSelectedContainer = onSelectedContainer,
        container = container,
        onContainer = onContainer,
        border = border,
        selectedBorder = selectedBorder,
    )

    @Composable
    @ReadOnlyComposable
    fun height(): Dp = HhHeightChipRow

    @Composable
    @ReadOnlyComposable
    fun selectedTick(): ImageVector = HhIcons.Check
}

@Preview(showBackground = true)
@Composable
private fun HhFilterChipPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhFilterChipPreviewRow()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhFilterChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhFilterChipPreviewRow()
    }
}

@Composable
private fun HhFilterChipPreviewRow() {
    Row(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhFilterChip(
            label = "Remote",
            selected = false,
            onClick = {},
        )
        HhFilterChip(
            label = "Remote",
            selected = true,
            onClick = {},
        )
        HhFilterChip(
            label = "Hybrid",
            selected = false,
            onClick = {},
            leadingIcon = HhIcons.Applications,
        )
        HhFilterChip(
            label = "Hybrid",
            selected = true,
            onClick = {},
            leadingIcon = HhIcons.Applications,
        )
        HhFilterChip(
            label = "Offers",
            selected = true,
            onClick = {},
            count = 12,
        )
        HhFilterChip(
            label = "Offers",
            selected = false,
            onClick = {},
            count = 12,
        )
    }
}
