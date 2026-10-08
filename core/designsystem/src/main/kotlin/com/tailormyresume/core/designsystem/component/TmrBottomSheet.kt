package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TmrBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    contentPadding: PaddingValues? = null,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TmrTheme.colors
    val padding = contentPadding
        ?: PaddingValues(start = TmrTheme.spacing.gutter, end = TmrTheme.spacing.gutter, bottom = TmrTheme.spacing.d24)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = TmrTheme.shapes.modalSheet,
        containerColor = colors.sheet,
        contentColor = colors.onSurface,
        scrimColor = colors.scrim,
        tonalElevation = 0.dp,
        dragHandle = { TmrBottomSheetDragHandle() },
    ) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            if (title != null) {
                Text(text = title, style = TmrTheme.typography.headlineM, color = colors.onSurface)
            }
            if (subtitle != null) {
                Text(text = subtitle, style = TmrTheme.typography.bodyM, color = colors.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun TmrBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = TmrTheme.spacing.md)
            .size(width = TmrSizeSheetHandleWidth, height = TmrSizeDragHandleHeight)
            .clip(TmrTheme.shapes.pill)
            .background(TmrTheme.colors.outlineVariant),
    )
}

@Composable
fun TmrSheetActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = TmrTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TmrRadiusSheetRow),
        color = colors.sheet,
        border = BorderStroke(TmrWidthStrokeSheetItem, colors.sheetItemBorder),
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = TmrHeightSheetRow)
                .padding(horizontal = TmrTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(TmrSizeMonogram).clip(TmrTheme.shapes.pill).background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(TmrSizeIcon))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = TmrTheme.typography.titleS, color = colors.onSurface)
                if (subtitle != null) {
                    Text(text = subtitle, style = TmrTheme.typography.bodyS, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

private val TmrHeightSheetRow = 64.dp
private val TmrRadiusSheetRow = 32.dp
private val TmrWidthStrokeSheetItem = 1.5.dp

private const val TMR_BOTTOM_SHEET_SAMPLE_TITLE = "Close the gap: Unit tests with JUnit"

@Preview(showBackground = true)
@Composable
private fun TmrSheetActionRowPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrSheetActionRowSample() }
}

@Preview(showBackground = true)
@Composable
private fun TmrSheetActionRowDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrSheetActionRowSample() }
}

@Composable
private fun TmrSheetActionRowSample() {
    Column(Modifier.padding(TmrTheme.spacing.lg)) {
        Text(text = TMR_BOTTOM_SHEET_SAMPLE_TITLE, style = TmrTheme.typography.titleL, color = TmrTheme.colors.onSurface)
        TmrSheetActionRow(
            icon = TmrIcons.Add,
            title = "Add to prep plan",
            subtitle = "A short task list, no deadline pressure",
            onClick = {},
        )
    }
}
