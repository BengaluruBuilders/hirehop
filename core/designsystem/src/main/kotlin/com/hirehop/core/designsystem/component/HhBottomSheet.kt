package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HhBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    contentPadding: PaddingValues? = null,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val padding = contentPadding
        ?: PaddingValues(
            start = HhTheme.spacing.d20,
            end = HhTheme.spacing.d20,
            bottom = HhTheme.spacing.d32,
        )
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = HhRadiusSheet, topEnd = HhRadiusSheet),
        containerColor = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        scrimColor = colors.scrim,
        dragHandle = { HhBottomSheetDragHandle() },
    ) {
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = HhTheme.typography.titleLarge,
                    color = colors.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun HhBottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = HhTheme.spacing.sm)
            .size(width = HhSizeDragHandleWidth, height = HhSizeDragHandleHeight)
            .background(
                color = HhTheme.colors.hairlineStrong,
                shape = RoundedCornerShape(HhTheme.shapes.full),
            ),
    )
}

private const val HH_BOTTOM_SHEET_SAMPLE_TITLE = "Pick a source"

@Preview(showBackground = true)
@Composable
private fun HhBottomSheetPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhBottomSheetSample()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhBottomSheetDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhBottomSheetSample()
    }
}

@Composable
private fun HhBottomSheetSample() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = HH_BOTTOM_SHEET_SAMPLE_TITLE,
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
    }
}
