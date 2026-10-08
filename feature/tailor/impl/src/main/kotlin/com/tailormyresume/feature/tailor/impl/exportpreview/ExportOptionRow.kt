package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ExportOptionRow(
    options: List<T>,
    selected: T,
    labelOf: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    groupDescription: String,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.card, RoundedCornerShape(TmrTheme.spacing.d24 + TmrTheme.spacing.d4))
            .padding(TmrTheme.spacing.d4)
            .selectableGroup()
            .semantics { contentDescription = groupDescription },
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs + TmrTheme.spacing.xxs),
    ) {
        options.forEach { option ->
            ExportOption(
                label = labelOf(option),
                selected = option == selected,
                onClick = { onSelect(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExportOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TmrTheme.colors
    val contentColor = if (selected) colors.onBrand else colors.onSurface
    Row(
        modifier = modifier
            .heightIn(min = TmrTheme.spacing.touch)
            .clip(TmrTheme.shapes.pill)
            .background(if (selected) colors.brand else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = TmrTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs + TmrTheme.spacing.xxs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Icon(
                imageVector = TmrIcons.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(TmrTheme.spacing.lg),
            )
        }
        Text(text = label, style = TmrTheme.typography.labelL, color = contentColor)
    }
}
