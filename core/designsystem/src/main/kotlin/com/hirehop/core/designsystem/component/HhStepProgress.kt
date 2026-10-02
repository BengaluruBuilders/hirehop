package com.hirehop.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhStepProgress(
    stepNames: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    ordinalLabel: String? = null,
    stepDetails: List<String?> = emptyList(),
    footnote: String? = null,
) {
    if (stepNames.isEmpty()) return
    val index = currentStepIndex.coerceIn(0, stepNames.size - 1)
    HhCardSurface(
        modifier = modifier,
        shape = HhTheme.shapes.card,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        trailingAction = null,
        fill = HhTheme.colors.document,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = ordinalLabel ?: hhStepOrdinal(index, stepNames.size),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            stepNames.forEachIndexed { position, name ->
                HhStepRow(name = name, detail = stepDetails.getOrNull(position), state = stepStateOf(position, index))
            }
            if (footnote != null) {
                Text(text = footnote, style = HhTheme.typography.bodyS, color = HhTheme.colors.onSurfaceVariant)
            }
        }
    }
}

private enum class HhStepState { Done, Active, Pending }

private fun stepStateOf(position: Int, current: Int): HhStepState = when {
    position < current -> HhStepState.Done
    position == current -> HhStepState.Active
    else -> HhStepState.Pending
}

@Composable
private fun HhStepRow(name: String, detail: String?, state: HhStepState) {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 44.dp),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        HhStepMark(state = state)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                style = if (state == HhStepState.Pending) {
                    HhTheme.typography.labelL
                } else {
                    HhTheme.typography.titleS
                },
                color = if (state == HhStepState.Pending) colors.onSurfaceVariant else colors.onSurface,
            )
            if (detail != null) {
                Text(text = detail, style = HhTheme.typography.bodyS, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HhStepMark(state: HhStepState) {
    val colors = HhTheme.colors
    when (state) {
        HhStepState.Done -> HhStatusDisc(kind = HhStatusKind.Met, size = HhSizeStepMark)
        HhStepState.Pending -> Canvas(Modifier.size(HhSizeStepMark).clearAndSetSemantics {}) {
            drawRing(colors.outline, dashed = true)
        }
        HhStepState.Active -> HhActiveArc(
            track = colors.outlineVariant,
            arc = colors.primary,
            size = HhSizeStepMark,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
internal fun HhActiveArc(
    track: Color,
    arc: Color,
    size: Dp,
    modifier: Modifier,
) {
    val rotation = if (HhTheme.motion.reduced) 0f else rememberArcRotation()
    Canvas(modifier.size(size)) {
        drawRing(track, dashed = false, width = ARC_STROKE, sweep = FULL_TURN)
        rotate(degrees = rotation, pivot = center) {
            drawRing(arc, dashed = false, width = ARC_STROKE, sweep = QUARTER_TURN, round = true)
        }
    }
}

@Composable
private fun rememberArcRotation(): Float {
    val transition = rememberInfiniteTransition(label = "hhArc")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(tween(ARC_TURN_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "hhArcRotation",
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

private fun hhStepOrdinal(index: Int, total: Int): String = "Step ${index + 1} of $total"

private val HhStepProgressSampleSteps: List<String> = listOf(
    "Reading the job description",
    "Matching your confirmed facts",
    "Drafting changes",
    "Checking every line has a source",
)

@Preview(showBackground = true)
@Composable
private fun HhStepProgressPreview() {
    HhPreviewTheme(darkTheme = false) { HhStepProgressPreviewBody() }
}

@Preview(showBackground = true)
@Composable
private fun HhStepProgressDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhStepProgressPreviewBody() }
}

@Composable
private fun HhStepProgressPreviewBody() {
    HhStepProgress(
        stepNames = HhStepProgressSampleSteps,
        currentStepIndex = 1,
        modifier = Modifier.padding(HhTheme.spacing.lg),
        stepDetails = listOf("14 requirements found", "Checking 23 facts"),
    )
}
