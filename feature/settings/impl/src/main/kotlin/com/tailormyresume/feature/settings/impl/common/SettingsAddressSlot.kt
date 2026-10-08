package com.tailormyresume.feature.settings.impl.common

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
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
internal fun SettingsAddressSlot(
    address: String,
    pendingLabel: String,
    modifier: Modifier = Modifier,
) {
    val lineColor = TmrTheme.colors.outline
    val corner = TmrTheme.spacing.sm
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
            .padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs),
    ) {
        Text(text = address, style = TmrTheme.typography.factId, color = TmrTheme.colors.onSurfaceVariant)
        Text(text = pendingLabel, style = TmrTheme.typography.labelM, color = TmrTheme.colors.onSurfaceVariant)
    }
}

private val SLOT_STROKE = 1.5.dp
private val SLOT_DASH = 4.dp
