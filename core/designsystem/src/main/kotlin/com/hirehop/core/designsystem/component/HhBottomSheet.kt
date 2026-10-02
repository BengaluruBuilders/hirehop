package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HhBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    contentPadding: PaddingValues? = null,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val padding = contentPadding
        ?: PaddingValues(start = HhTheme.spacing.gutter, end = HhTheme.spacing.gutter, bottom = HhTheme.spacing.d24)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = HhTheme.shapes.sheet,
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        scrimColor = colors.scrim,
        tonalElevation = 0.dp,
        dragHandle = { HhBottomSheetDragHandle() },
    ) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            if (title != null) {
                Text(text = title, style = HhTheme.typography.titleL, color = colors.onSurface)
            }
            if (subtitle != null) {
                Text(text = subtitle, style = HhTheme.typography.bodyM, color = colors.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun HhBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = HhTheme.spacing.md)
            .size(width = HhSizeSheetHandleWidth, height = HhSizeDragHandleHeight)
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.outline),
    )
}

@Composable
fun HhSheetActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = HhTheme.colors
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), color = colors.surface) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = HHHeightSheetRow)
                .padding(horizontal = HhTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(HhSizeMonogram).clip(HhTheme.shapes.pill).background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(HhSizeIcon))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = HhTheme.typography.titleS, color = colors.onSurface)
                if (subtitle != null) {
                    Text(text = subtitle, style = HhTheme.typography.bodyS, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

private val HHHeightSheetRow = 56.dp

private const val HH_BOTTOM_SHEET_SAMPLE_TITLE = "Close the gap: Unit tests with JUnit"

@Preview(showBackground = true)
@Composable
private fun HhSheetActionRowPreview() {
    HhPreviewTheme(darkTheme = false) { HhSheetActionRowSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhSheetActionRowDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhSheetActionRowSample() }
}

@Composable
private fun HhSheetActionRowSample() {
    Column(Modifier.padding(HhTheme.spacing.lg)) {
        Text(text = HH_BOTTOM_SHEET_SAMPLE_TITLE, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
        HhSheetActionRow(
            icon = HhIcons.Add,
            title = "Add to prep plan",
            subtitle = "A short task list, no deadline pressure",
            onClick = {},
        )
    }
}
