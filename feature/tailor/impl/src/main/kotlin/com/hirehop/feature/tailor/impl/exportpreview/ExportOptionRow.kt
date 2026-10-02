package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

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
            .selectableGroup()
            .semantics { contentDescription = groupDescription },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
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
    val colors = HhTheme.colors
    val contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    Box(
        modifier = modifier
            .heightIn(min = HhTheme.spacing.touch)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d40)
                .clip(HhTheme.shapes.pill)
                .background(if (selected) colors.primaryContainer else colors.card)
                .then(
                    if (selected) {
                        Modifier
                    } else {
                        Modifier.border(HhTheme.spacing.d2, colors.outlineVariant, HhTheme.shapes.pill)
                    },
                )
                .padding(horizontal = HhTheme.spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) {
                    Icon(
                        imageVector = HhIcons.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(HhTheme.spacing.lg),
                    )
                }
                Text(text = label, style = HhTheme.typography.labelL, color = contentColor)
            }
        }
    }
}
