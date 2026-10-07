package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.hirehop.core.designsystem.theme.HhTheme
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
private fun HhHomeHeaderHeadline(headline: String, action: (@Composable () -> Unit)?, hasIllustration: Boolean) {
    Column(
        modifier = Modifier
            .padding(horizontal = HhTheme.spacing.gutter)
            .fillMaxWidth(if (hasIllustration) HEADLINE_FRACTION else 1f)
            .padding(top = HhTheme.spacing.d24, bottom = HhOverlap.Sheet + HhTheme.spacing.d24),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d16),
    ) {
        HhHeadline(text = headline, style = HhTheme.typography.headlineL, color = HhTheme.colors.onHeader)
        if (action != null) {
            action()
        }
    }
}

@Composable
fun HhCollapsingHomeHeader(
    collapse: HhHeaderCollapseState,
    title: String,
    greeting: String,
    headline: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
    action: (@Composable () -> Unit)? = null,
    illustration: (@Composable BoxScope.() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val gutter = HhTheme.spacing.gutter
    val titleGap = HhTheme.spacing.sm
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val parallax = if (HhTheme.motion.reduced) 0f else COLLAPSE_PARALLAX
    Layout(
        content = {
            Text(text = greeting, style = HhTheme.typography.titleM, color = colors.onHeader)
            Text(text = title, style = HhTheme.typography.titleL, color = colors.onHeader)
            HhHomeHeaderHeadline(headline, action, illustration != null)
            Box(if (illustration == null) Modifier.size(0.dp) else Modifier.size(ILLUSTRATION_WIDTH, ILLUSTRATION_HEIGHT)) {
                illustration?.invoke(this)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(titleGap),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .layoutId(HhHeaderData(drawsAboveContent = true, overlap = HhOverlap.Sheet))
            .hhHeaderBackdrop(colors.header, HhOverlap.Sheet),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val side = gutter.roundToPx()
        val status = statusTop.roundToPx()
        val overlap = HhOverlap.Sheet.roundToPx()
        val loose = Constraints(maxWidth = width)
        val pill = measurables[4].measure(Constraints(maxWidth = (width - 2 * side).coerceAtLeast(0)))
        val stacked = largeText || pill.width > (width - 2 * side) * 0.6f
        val textWidth = (if (stacked) width - 2 * side else width - 2 * side - pill.width).coerceAtLeast(0)
        val hello = measurables[0].measure(Constraints.fixedWidth(textWidth))
        val name = measurables[1].measure(Constraints.fixedWidth((textWidth - titleGap.roundToPx()).coerceAtLeast(0)))
        val text = measurables[2].measure(loose)
        val hero = measurables[3].measure(loose)
        val rowTop = (statusTop + GREETING_TOP).roundToPx()
        val gap = titleGap.roundToPx()
        val rowHeight = if (stacked) hello.height + gap + pill.height else max(hello.height, pill.height)
        val textTop = rowTop + rowHeight
        val expanded = max(textTop + text.height, (HhHeightHomeHeader + statusTop).roundToPx())
        val barContent = status + (if (stacked) name.height + gap + pill.height else max(name.height, pill.height))
        val barTotal = barContent + overlap
        val bar = max(barTotal, (HhHeightCollapsedHomeHeader + statusTop).roundToPx()) - status - overlap
        collapse.range = (expanded - status - bar - overlap).coerceAtLeast(0).toFloat()
        layout(width, expanded + collapse.offset.roundToInt()) {
            val fraction = collapse.fraction
            if (fraction < COLLAPSE_FADE_SWITCH) {
                hello.placeRelativeWithLayer(side, rowTop + if (stacked) 0 else centered(hello.height, rowHeight)) {
                    alpha = collapse.heroAlpha
                }
                text.placeRelativeWithLayer(0, textTop) { behindSheet(collapse, expanded - overlap - textTop, parallax) }
                hero.placeRelativeWithLayer(width - hero.width, (statusTop + ILLUSTRATION_TOP).roundToPx()) {
                    behindSheet(collapse, hero.height, parallax)
                }
            } else {
                name.placeRelativeWithLayer(side, status + if (stacked) 0 else centered(name.height, bar)) {
                    alpha = (collapse.fraction - COLLAPSE_FADE_SWITCH) / (1f - COLLAPSE_FADE_SWITCH)
                }
            }
            val pillTop = if (stacked) {
                lerp(rowTop + hello.height + gap, status + name.height + gap, fraction)
            } else {
                lerp(rowTop + centered(pill.height, rowHeight), status + centered(pill.height, bar), fraction)
            }
            pill.placeRelative(width - side - pill.width, pillTop)
        }
    }
}

private fun centered(size: Int, space: Int): Int = Alignment.CenterVertically.align(size, space)

private val HhHeaderCollapseState.heroAlpha: Float
    get() = (1f - fraction / COLLAPSE_FADE_SWITCH).coerceAtLeast(0f)

private fun GraphicsLayerScope.behindSheet(collapse: HhHeaderCollapseState, restingBottom: Int, parallax: Float) {
    val bottom = (restingBottom + collapse.offset * (1f - parallax)).coerceAtLeast(0f)
    alpha = if (bottom > 0f) collapse.heroAlpha else 0f
    translationY = collapse.offset * parallax
    clip = collapse.fraction > 0f
    shape = HhTopRect(bottom)
}

private class HhTopRect(private val bottom: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rectangle(Rect(0f, 0f, size.width, bottom))
}

private val GREETING_TOP = 12.dp
private val ILLUSTRATION_TOP = 86.dp
private val ILLUSTRATION_WIDTH = 174.dp
private val ILLUSTRATION_HEIGHT = 200.dp
private const val HEADLINE_FRACTION = 0.55f
private const val COLLAPSE_FADE_SWITCH = 0.5f
private const val COLLAPSE_PARALLAX = 0.5f

@Preview(showBackground = true)
@Composable
private fun HhCollapsingHomeHeaderPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhCollapsingHomeHeader(
            collapse = HhHeaderCollapseState(initialFraction = 1f),
            title = "Applications",
            greeting = "Hi, Priya",
            headline = "Your facts, every job",
            trailing = { Text("4 left") },
        )
    }
}
