package com.hirehop.feature.profile.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.R

private val ChipHeight = 28.dp
private val ChipIconSize = 16.dp
private val DashOn = 4.dp
private val DashOff = 3.dp
private val DashStroke = 1.5.dp

@Composable
internal fun FactIdChip(
    id: String,
    status: FactStatus,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val container = status.container(colors)
    val content = status.content(colors)
    val statusLabel = stringResource(status.labelRes())
    val description = stringResource(R.string.feature_profile_impl_fact_chip_description, id, statusLabel)
    val shape = HhTheme.shapes.pill
    val dashed = status == FactStatus.ToConfirm
    Row(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .height(ChipHeight)
            .background(container, shape)
            .then(if (dashed) Modifier.dashedBorder(colors.outline) else Modifier)
            .padding(horizontal = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        status.icon()?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(ChipIconSize),
            )
        }
        Text(text = id, style = HhTheme.typography.factId, color = content)
    }
}

@Composable
internal fun ToConfirmChip(
    label: String,
    modifier: Modifier = Modifier,
    onHeader: Boolean = false,
) {
    val colors = HhTheme.colors
    val content = if (onHeader) colors.onHeader else colors.onSurfaceVariant
    val line = if (onHeader) colors.onHeader.copy(alpha = HEADER_LINE_ALPHA) else colors.outline
    val fill = if (onHeader) colors.onHeader.copy(alpha = HEADER_FILL_ALPHA) else Color.Transparent
    Row(
        modifier = modifier
            .height(ChipHeight)
            .background(fill, HhTheme.shapes.pill)
            .dashedBorder(line)
            .padding(horizontal = HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = HhTheme.typography.labelM, color = content)
    }
}

private const val HEADER_LINE_ALPHA = 0.7f
private const val HEADER_FILL_ALPHA = 0.12f

internal fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    val stroke = Stroke(
        width = DashStroke.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn.toPx(), DashOff.toPx())),
    )
    val half = stroke.width / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(half, half),
        size = Size(size.width - stroke.width, size.height - stroke.width),
        cornerRadius = CornerRadius(size.height / 2),
        style = stroke,
    )
}

private fun FactStatus.container(colors: HhColors): Color = when (this) {
    FactStatus.Confirmed -> colors.metContainer
    FactStatus.UserStated -> colors.partialContainer
    FactStatus.UserEdited, FactStatus.Scanned -> colors.neutralContainer
    FactStatus.ToConfirm -> Color.Transparent
}

private fun FactStatus.content(colors: HhColors): Color = when (this) {
    FactStatus.Confirmed -> colors.onMetContainer
    FactStatus.UserStated -> colors.onPartialContainer
    FactStatus.UserEdited, FactStatus.Scanned -> colors.onNeutralContainer
    FactStatus.ToConfirm -> colors.onSurfaceVariant
}

private fun FactStatus.icon(): ImageVector? = when (this) {
    FactStatus.Confirmed -> HhIcons.Check
    FactStatus.UserStated -> HhIcons.Chat
    FactStatus.UserEdited -> HhIcons.Edit
    FactStatus.Scanned -> HhIcons.Scan
    FactStatus.ToConfirm -> null
}
