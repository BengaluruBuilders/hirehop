package com.hirehop.feature.tailor.impl.credits

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import com.hirehop.core.designsystem.theme.HhTheme
import kotlin.math.roundToInt

private const val ROLLED_OUT_ALPHA = 0.25f

@Composable
internal fun CreditCounter(
    credits: Int,
    previousCredits: Int,
    suffix: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    style: TextStyle = HhTheme.typography.numeralHero,
) {
    val colors = HhTheme.colors
    val density = LocalDensity.current
    val lineHeight = with(density) { style.lineHeight.toPx() }.coerceAtLeast(1f)
    val hop = HhTheme.motion.hopSpecs.spatial
    val offset = remember(credits, previousCredits) { Animatable(if (credits == previousCredits) 0f else 1f) }
    LaunchedEffect(credits, previousCredits) {
        if (offset.value != 0f) offset.animateTo(targetValue = 0f, animationSpec = hop)
    }
    Row(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            liveRegion = LiveRegionMode.Polite
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.height(with(density) { lineHeight.toDp() }).clipToBounds()) {
            Text(
                text = previousCredits.toString(),
                style = style,
                color = colors.onSurface.copy(alpha = ROLLED_OUT_ALPHA),
                maxLines = 1,
                modifier = Modifier.offset { IntOffset(0, ((offset.value - 1f) * lineHeight).roundToInt()) },
            )
            Text(
                text = credits.toString(),
                style = style,
                color = colors.onSurface,
                maxLines = 1,
                modifier = Modifier.offset { IntOffset(0, (offset.value * lineHeight).roundToInt()) },
            )
        }
        if (suffix.isNotEmpty()) {
            Text(text = " $suffix", style = style, color = colors.onSurface, maxLines = 1)
        }
    }
}
