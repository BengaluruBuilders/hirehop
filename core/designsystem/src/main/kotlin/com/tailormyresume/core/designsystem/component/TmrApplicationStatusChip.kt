package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrColors
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
@Suppress("UNUSED_PARAMETER")
fun TmrApplicationStatusChip(
    kind: TmrApplicationStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
    dotSize: Dp = TmrSizeDisc,
) {
    val colors = TmrTheme.colors
    val content = colors.applicationStatusContent(kind)
    TmrPill(
        container = colors.applicationStatusContainer(kind),
        content = content,
        height = TmrHeightChip,
        modifier = modifier,
    ) {
        Icon(
            imageVector = kind.glyph(),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(TmrSizeChipIcon),
        )
        Text(text = label ?: kind.defaultLabel(), style = TmrTheme.typography.labelM, color = content)
    }
}

internal fun TmrColors.applicationStatusContainer(kind: TmrApplicationStatusKind): Color = when (kind) {
    TmrApplicationStatusKind.Offer -> metContainer
    else -> neutralContainer
}

internal fun TmrColors.applicationStatusContent(kind: TmrApplicationStatusKind): Color = when (kind) {
    TmrApplicationStatusKind.Interview -> primary
    TmrApplicationStatusKind.Offer -> onMetContainer
    else -> onNeutralContainer
}

private fun TmrApplicationStatusKind.glyph(): ImageVector = when (this) {
    TmrApplicationStatusKind.Saved -> TmrIcons.Bookmark
    TmrApplicationStatusKind.Applied -> TmrIcons.Send
    TmrApplicationStatusKind.Interview -> TmrIcons.Calendar
    TmrApplicationStatusKind.Offer -> TmrIcons.Award
    TmrApplicationStatusKind.Rejected -> TmrIcons.CancelCircle
    TmrApplicationStatusKind.NoResponse -> TmrIcons.Clock
}

@Preview(showBackground = true)
@Composable
private fun TmrApplicationStatusChipPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrApplicationStatusChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun TmrApplicationStatusChipDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrApplicationStatusChipPreviewColumn() }
}

@Composable
private fun TmrApplicationStatusChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrApplicationStatusKind.entries.forEach { kind -> TmrApplicationStatusChip(kind = kind) }
    }
}
