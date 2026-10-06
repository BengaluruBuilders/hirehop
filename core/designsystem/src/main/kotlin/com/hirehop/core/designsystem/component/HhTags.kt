package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhEditTypeTag(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    HhPill(
        container = colors.card,
        content = colors.onSurface,
        height = HhHeightTag,
        modifier = modifier,
        border = BorderStroke(HhWidthStroke, colors.outline),
    ) {
        Text(text = label, style = HhTheme.typography.labelM, color = colors.onSurface)
    }
}

@Composable
fun HhRequirementTag(
    label: String,
    mustHave: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val ink = if (mustHave) colors.onSurface else colors.onSurfaceVariant
    val frame = if (mustHave) {
        modifier.heightIn(min = HhHeightTagSmall)
    } else {
        modifier
            .heightIn(min = HhHeightTagSmall)
            .hhDashedBorder(colors.outline, HhWidthStroke, HhHeightTagSmall / 2)
    }
    HhPill(
        container = androidx.compose.ui.graphics.Color.Transparent,
        content = ink,
        height = HhHeightTagSmall,
        modifier = frame,
        border = if (mustHave) BorderStroke(HhWidthStroke, colors.onSurface) else null,
    ) {
        Text(text = label, style = HhTheme.typography.labelM, color = ink)
    }
}

@Composable
fun HhTermChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    HhPill(
        container = colors.metContainer,
        content = colors.onMetContainer,
        height = HhHeightChip,
        modifier = modifier,
    ) {
        Icon(
            imageVector = HhIcons.Check,
            contentDescription = null,
            tint = colors.onMetContainer,
            modifier = Modifier.size(HhSizeChipIcon),
        )
        Text(text = label, style = HhTheme.typography.labelM, color = colors.onMetContainer)
    }
}

@Composable
fun HhFactId(
    id: String,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    Text(
        text = id,
        modifier = modifier
            .background(colors.ground, HhTheme.shapes.pill)
            .border(HhWidthHairline, colors.outlineVariant, HhTheme.shapes.pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = HhTheme.typography.factId,
        color = colors.onSurface,
    )
}

@Preview(showBackground = true)
@Composable
private fun HhTagsPreview() {
    HhPreviewTheme(darkTheme = false) { HhTagsPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhTagsDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhTagsPreviewColumn() }
}

@Composable
private fun HhTagsPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhEditTypeTag(label = "Reword")
            HhRequirementTag(label = "Must-have", mustHave = true)
            HhRequirementTag(label = "Nice to have", mustHave = false)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhTermChip(label = "Kotlin")
            HhFactId(id = "F-031")
        }
    }
}
