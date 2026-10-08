package com.tailormyresume.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrStepProgress(
    stepNames: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    ordinalLabel: String? = null,
    stepDetails: List<String?> = emptyList(),
    footnote: String? = null,
    stepStatuses: List<String?> = emptyList(),
) {
    if (stepNames.isEmpty()) return
    val index = currentStepIndex.coerceIn(0, stepNames.size - 1)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        val ordinal = ordinalLabel ?: tmrStepOrdinal(index, stepNames.size)
        if (ordinal.isNotEmpty()) {
            Text(text = ordinal, style = TmrTheme.typography.labelM, color = TmrTheme.colors.onSurfaceVariant)
        }
        stepNames.forEachIndexed { position, name ->
            TmrStepRow(
                name = name,
                detail = stepDetails.getOrNull(position),
                status = stepStatuses.getOrNull(position),
                state = stepStateOf(position, index),
            )
        }
        if (footnote != null) {
            Text(text = footnote, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
        }
    }
}

private enum class TmrStepState { Done, Active, Pending }

private fun stepStateOf(position: Int, current: Int): TmrStepState = when {
    position < current -> TmrStepState.Done
    position == current -> TmrStepState.Active
    else -> TmrStepState.Pending
}

@Composable
private fun TmrStepRow(name: String, detail: String?, status: String?, state: TmrStepState) {
    val colors = TmrTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, TmrTheme.shapes.statusRow)
            .defaultMinSize(minHeight = TmrHeightStepRow)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrStepMark(state = state)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                style = if (state == TmrStepState.Pending) {
                    TmrTheme.typography.labelL
                } else {
                    TmrTheme.typography.titleS
                },
                color = if (state == TmrStepState.Pending) colors.onSurfaceVariant else colors.onSurface,
            )
            if (detail != null) {
                Text(text = detail, style = TmrTheme.typography.bodyS, color = colors.onSurfaceVariant)
            }
        }
        if (status != null) {
            Text(text = status, style = TmrTheme.typography.labelM, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun TmrStepMark(state: TmrStepState) {
    val colors = TmrTheme.colors
    when (state) {
        TmrStepState.Done -> Box(
            modifier = Modifier.size(TmrSizeStepMark).background(colors.brand, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(TmrIcons.Check, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(16.dp))
        }
        TmrStepState.Pending -> Canvas(Modifier.size(TmrSizeStepMark).clearAndSetSemantics {}) {
            drawRing(colors.outline, dashed = true)
        }
        TmrStepState.Active -> TmrActiveArc(
            track = colors.outlineVariant,
            arc = colors.primary,
            size = TmrSizeStepMark,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
internal fun TmrActiveArc(
    track: Color,
    arc: Color,
    size: Dp,
    modifier: Modifier,
) {
    val rotation = if (TmrTheme.motion.reduced) 0f else rememberArcRotation()
    Canvas(modifier.size(size)) {
        drawRing(track, dashed = false, width = ARC_STROKE, sweep = FULL_TURN)
        rotate(degrees = rotation, pivot = center) {
            drawRing(arc, dashed = false, width = ARC_STROKE, sweep = QUARTER_TURN, round = true)
        }
    }
}

@Composable
private fun rememberArcRotation(): Float {
    val transition = rememberInfiniteTransition(label = "tmrArc")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(tween(ARC_TURN_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "tmrArcRotation",
    )
    return rotation
}

private fun DrawScope.drawRing(
    color: Color,
    dashed: Boolean,
    width: Float = 2f,
    sweep: Float = FULL_TURN,
    round: Boolean = false,
) {
    val unit = size.minDimension / STEP_VIEWPORT
    val stroke = width * unit
    val pad = RING_INSET * unit
    val dash = if (dashed) PathEffect.dashPathEffect(floatArrayOf(stroke * 1.5f, stroke * 1.5f)) else null
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(pad, pad),
        size = Size(size.width - 2 * pad, size.height - 2 * pad),
        style = Stroke(width = stroke, cap = if (round) StrokeCap.Round else StrokeCap.Butt, pathEffect = dash),
    )
}

private const val STEP_VIEWPORT = 24f
private const val RING_INSET = 2.5f
private const val ARC_STROKE = 2.5f
private const val FULL_TURN = 360f
private const val QUARTER_TURN = 90f
private const val ARC_TURN_MS = 1200

private fun tmrStepOrdinal(index: Int, total: Int): String = "Step ${index + 1} of $total"

private val TmrStepProgressSampleSteps: List<String> = listOf(
    "Reading the job description",
    "Matching your confirmed facts",
    "Drafting changes",
    "Checking every line has a source",
)

@Preview(showBackground = true)
@Composable
private fun TmrStepProgressPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrStepProgressPreviewBody() }
}

@Preview(showBackground = true)
@Composable
private fun TmrStepProgressDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrStepProgressPreviewBody() }
}

@Composable
private fun TmrStepProgressPreviewBody() {
    TmrStepProgress(
        stepNames = TmrStepProgressSampleSteps,
        currentStepIndex = 1,
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        stepDetails = listOf("14 requirements found", "Checking 23 facts"),
    )
}
