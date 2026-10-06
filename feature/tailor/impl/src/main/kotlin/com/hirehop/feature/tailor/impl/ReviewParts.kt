package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

internal fun Modifier.dashedBorder(color: Color, width: Dp, radius: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF))),
    )
}

private const val DASH_ON = 14f
private const val DASH_OFF = 10f

@Composable
internal fun DecisionChip(state: BulletReviewState, modifier: Modifier = Modifier) {
    val labelRes = when (state) {
        BulletReviewState.TO_REVIEW -> R.string.feature_tailor_impl_chip_changed
        BulletReviewState.FLAGGED -> R.string.feature_tailor_impl_chip_verb_kept
        else -> state.labelRes() ?: return
    }
    val colors = HhTheme.colors
    val icon = when (state) {
        BulletReviewState.ACCEPTED -> HhIcons.Check
        BulletReviewState.FLAGGED -> HhIcons.Flag
        BulletReviewState.TO_REVIEW, BulletReviewState.USER_EDITED -> HhIcons.Edit
        else -> null
    }
    val container: Color
    val content: Color
    when (state) {
        BulletReviewState.ACCEPTED -> {
            container = colors.metContainer
            content = colors.onMetContainer
        }
        BulletReviewState.TO_REVIEW, BulletReviewState.USER_EDITED, BulletReviewState.FLAGGED -> {
            container = colors.special
            content = colors.onSpecial
        }
        else -> {
            container = colors.neutralContainer
            content = colors.onNeutralContainer
        }
    }
    Row(
        modifier = modifier
            .heightIn(min = HhTheme.spacing.d20 + HhTheme.spacing.d2)
            .clip(HhTheme.shapes.pill)
            .background(container)
            .padding(start = HhTheme.spacing.d4 + HhTheme.spacing.d2, end = HhTheme.spacing.d8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(HhTheme.spacing.d12),
            )
        }
        Text(
            text = stringResource(labelRes),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = content,
        )
    }
}

@Composable
internal fun DecideNote(modifier: Modifier = Modifier) {
    val colors = HhTheme.colors
    HhCard(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = HhTheme.spacing.cardPadding,
            vertical = HhTheme.spacing.d12,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
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
}
