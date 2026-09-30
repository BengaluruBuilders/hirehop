package com.hirehop.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhStepProgress(
    stepNames: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    ordinalLabel: String? = null,
) {
    if (stepNames.isEmpty()) return
    val colors = HhTheme.colors
    val index = currentStepIndex.coerceIn(0, stepNames.size - 1)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = ordinalLabel ?: hhStepOrdinal(index, stepNames.size),
            style = HhTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = stepNames[index],
            style = HhTheme.typography.titleMedium,
            color = colors.onSurface,
        )
        HhStepProgressTrack()
        HhStepProgressNames(stepNames = stepNames, currentIndex = index)
    }
}

@Composable
private fun HhStepProgressTrack() {
    val reducedMotion = hhReducedMotion()
    val proofMs = HhTheme.motion.proof
    val transition = rememberInfiniteTransition(label = "hhStepProgressTrack")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = proofMs),
            repeatMode = RepeatMode.Restart,
        ),
        label = "hhStepProgressSweep",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HhWidthHairline)
            .clip(RoundedCornerShape(HhTheme.shapes.xs))
            .graphicsLayer {
                scaleX = if (reducedMotion) HH_STEP_PROGRESS_STATIC_SWEEP else sweep
                transformOrigin = TransformOrigin(0f, 0.5f)
            }
            .background(color = HhTheme.colors.primary),
    )
}

@Composable
private fun HhStepProgressNames(stepNames: List<String>, currentIndex: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        stepNames.forEachIndexed { index, name ->
            val isCurrent = index == currentIndex
            Text(
                text = name,
                style = HhTheme.typography.bodySmall,
                color = if (isCurrent) {
                    HhTheme.colors.onSurface
                } else {
                    HhTheme.colors.onSurfaceVariant
                },
            )
        }
    }
}

private const val HH_STEP_PROGRESS_STATIC_SWEEP: Float = 0.35f

private fun hhStepOrdinal(index: Int, total: Int): String = "Step ${index + 1} of $total"

private val HhStepProgressSampleSteps: List<String> = listOf(
    "Reading the JD",
    "Matching your facts",
    "Checking every line",
)

@Preview(showBackground = true)
@Composable
private fun HhStepProgressPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhStepProgress(
            stepNames = HhStepProgressSampleSteps,
            currentStepIndex = 1,
            modifier = Modifier.padding(HhTheme.spacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhStepProgressDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhStepProgress(
            stepNames = HhStepProgressSampleSteps,
            currentStepIndex = 1,
            modifier = Modifier.padding(HhTheme.spacing.lg),
        )
    }
}
