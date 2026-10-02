package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

internal fun Modifier.dashedBorder(color: Color, width: Dp, radius: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF))),
    )
}

private const val DASH_ON = 14f
private const val DASH_OFF = 10f

@Composable
internal fun DecisionChip(state: BulletReviewState, modifier: Modifier = Modifier) {
    val labelRes = state.labelRes() ?: return
    val colors = HhTheme.colors
    val container: Color
    val content: Color
    val icon = when (state) {
        BulletReviewState.ACCEPTED -> HhIcons.Check
        BulletReviewState.FLAGGED -> HhIcons.Flag
        BulletReviewState.USER_EDITED -> HhIcons.Edit
        else -> null
    }
    when (state) {
        BulletReviewState.ACCEPTED -> {
            container = colors.metContainer
            content = colors.onMetContainer
        }
        BulletReviewState.TO_REVIEW -> {
            container = Color.Transparent
            content = colors.onSurfaceVariant
        }
        else -> {
            container = colors.neutralContainer
            content = colors.onNeutralContainer
        }
    }
    val shape = HhTheme.shapes.pill
    val base = modifier
        .height(HhTheme.spacing.d24)
        .background(container, shape)
    val framed = if (state == BulletReviewState.TO_REVIEW) {
        base.dashedBorder(colors.outline, HhTheme.spacing.d2, HhTheme.spacing.d12)
    } else {
        base
    }
    Row(
        modifier = framed.padding(horizontal = HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(HhTheme.spacing.d12))
        }
        Text(text = stringResource(labelRes), style = HhTheme.typography.labelM, color = content)
    }
}

@Composable
internal fun SourceBadge(
    factIds: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    if (factIds.isEmpty()) return
    val description = stringResource(R.string.feature_tailor_impl_source_description, factIds.joinToString(", "))
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = HhTheme.spacing.touch, minHeight = HhTheme.spacing.touch)
            .clickable(onClick = onClick, role = Role.Button)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            modifier = Modifier
                .clearAndSetSemantics {}
                .background(if (selected) HhTheme.colors.primaryContainer else Color.Transparent, HhTheme.shapes.tag)
                .border(BorderStroke(HhTheme.spacing.d2 * 0.75f, HhTheme.colors.primary), HhTheme.shapes.tag)
                .padding(horizontal = HhTheme.spacing.xs + HhTheme.spacing.xxs, vertical = HhTheme.spacing.d4 - 1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            factIds.forEach { id ->
                Text(text = id, style = HhTheme.typography.factId, color = HhTheme.colors.onSurface)
            }
        }
    }
}

@Composable
internal fun DecideNote(modifier: Modifier = Modifier) {
    val colors = HhTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.card)
            .border(BorderStroke(HhTheme.spacing.d2 / 2, colors.outlineVariant), HhTheme.shapes.card)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.d12 - HhTheme.spacing.d2),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Verified,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(HhTheme.spacing.d20),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_decide_note),
            style = HhTheme.typography.bodyM,
            color = colors.onSurface,
        )
    }
}
