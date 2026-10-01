package com.hirehop.feature.tailor.impl.exported

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.hirehop.core.designsystem.theme.HhTheme
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

private const val EXPORTED_TABULAR_FIGURES = "tnum"

private const val EXPORTED_ROLLED_OUT_ALPHA = 0.25f

@Composable
internal fun ExportedCreditNumeral(
    credits: Int,
    previousCredits: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val style = HhTheme.typography.heroNumeral.copy(fontFeatureSettings = EXPORTED_TABULAR_FIGURES)
    val colors = HhTheme.colors
    val density = LocalDensity.current
    val lineHeight = with(density) { style.lineHeight.toPx() }.coerceAtLeast(1f)
    val hopMillis = HhTheme.motion.hop
    val easing = HhTheme.motion.emphasized
    val settled = credits == previousCredits
    val offset = remember(credits, previousCredits) { Animatable(if (settled) 0f else 1f) }
    LaunchedEffect(credits, previousCredits) {
        if (offset.value == 0f) return@LaunchedEffect
        if (coroutineContext[MotionDurationScale.Key]?.scaleFactor == 0f) return@LaunchedEffect
        offset.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = hopMillis,
                easing = easing,
            ),
        )
    }
    Box(
        modifier = modifier
            .height(with(density) { lineHeight.toDp() })
            .clipToBounds()
            .semantics {
                this.contentDescription = contentDescription
                liveRegion = LiveRegionMode.Polite
            },
    ) {
        Text(
            text = previousCredits.toString(),
            style = style,
            color = colors.onSurface.copy(alpha = EXPORTED_ROLLED_OUT_ALPHA),
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier = Modifier.offset { IntOffset(0, ((offset.value - 1f) * lineHeight).roundToInt()) },
        )
        Text(
            text = credits.toString(),
            style = style,
            color = colors.onSurface,
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier = Modifier.offset { IntOffset(0, (offset.value * lineHeight).roundToInt()) },
        )
    }
}
