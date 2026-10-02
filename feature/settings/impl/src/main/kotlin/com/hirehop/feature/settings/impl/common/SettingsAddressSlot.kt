package com.hirehop.feature.settings.impl.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun SettingsAddressSlot(
    address: String,
    pendingLabel: String,
    modifier: Modifier = Modifier,
) {
    val lineColor = HhTheme.colors.outline
    val corner = HhTheme.spacing.sm
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .drawBehind {
                val dash = SLOT_DASH.toPx()
                drawRoundRect(
                    color = lineColor,
                    cornerRadius = CornerRadius(corner.toPx()),
                    style = Stroke(
                        width = SLOT_STROKE.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
                    ),
                )
            }
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(text = address, style = HhTheme.typography.factId, color = HhTheme.colors.onSurfaceVariant)
        Text(text = pendingLabel, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
    }
}

private val SLOT_STROKE = 1.5.dp
private val SLOT_DASH = 4.dp
