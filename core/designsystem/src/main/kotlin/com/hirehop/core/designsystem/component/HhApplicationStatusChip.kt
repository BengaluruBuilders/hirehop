package com.hirehop.core.designsystem.component

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
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
@Suppress("UNUSED_PARAMETER")
fun HhApplicationStatusChip(
    kind: HhApplicationStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
    dotSize: Dp = HhSizeDisc,
) {
    val colors = HhTheme.colors
    val content = colors.applicationStatusContent(kind)
    HhPill(
        container = colors.applicationStatusContainer(kind),
        content = content,
        height = HhHeightChip,
        modifier = modifier,
    ) {
        Icon(
            imageVector = kind.glyph(),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(HhSizeChipIcon),
        )
        Text(text = label ?: kind.defaultLabel(), style = HhTheme.typography.labelM, color = content)
    }
}

private fun HhColors.applicationStatusContainer(kind: HhApplicationStatusKind): Color = when (kind) {
    HhApplicationStatusKind.Interview -> partialContainer
    HhApplicationStatusKind.Offer -> metContainer
    else -> neutralContainer
}

private fun HhColors.applicationStatusContent(kind: HhApplicationStatusKind): Color = when (kind) {
    HhApplicationStatusKind.Interview -> onPartialContainer
    HhApplicationStatusKind.Offer -> onMetContainer
    else -> onNeutralContainer
}

private fun HhApplicationStatusKind.glyph(): ImageVector = when (this) {
    HhApplicationStatusKind.Saved -> HhIcons.Bookmark
    HhApplicationStatusKind.Applied -> HhIcons.Send
    HhApplicationStatusKind.Interview -> HhIcons.Calendar
    HhApplicationStatusKind.Offer -> HhIcons.Award
    HhApplicationStatusKind.Rejected -> HhIcons.CancelCircle
    HhApplicationStatusKind.NoResponse -> HhIcons.Clock
}

@Preview(showBackground = true)
@Composable
private fun HhApplicationStatusChipPreview() {
    HhPreviewTheme(darkTheme = false) { HhApplicationStatusChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhApplicationStatusChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhApplicationStatusChipPreviewColumn() }
}

@Composable
private fun HhApplicationStatusChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhApplicationStatusKind.entries.forEach { kind -> HhApplicationStatusChip(kind = kind) }
    }
}
