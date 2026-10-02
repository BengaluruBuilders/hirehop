package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow

internal const val HH_STACKED_FONT_SCALE: Float = 1.5f

@Composable
fun HhBottomActionBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    creditDisclosure: (@Composable () -> Unit)? = null,
    stacked: Boolean = LocalDensity.current.fontScale >= HH_STACKED_FONT_SCALE,
    primaryLast: Boolean = true,
    actions: @Composable RowScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val shape = if (stacked) HhTheme.shapes.heroCard else HhTheme.shapes.pill
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(contentPadding ?: PaddingValues(horizontal = HhTheme.spacing.gutter))
            .padding(bottom = HhOuterGapBar),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        if (creditDisclosure != null) {
            creditDisclosure()
        }
        Surface(
            modifier = Modifier
                .widthIn(max = HhMaxWidthDock)
                .fillMaxWidth()
                .hhShadow(HhTheme.elevation.dock, shape),
            shape = shape,
            color = colors.tool,
            contentColor = colors.onTool,
            border = if (HhTheme.isDark) BorderStroke(HhWidthHairline, colors.outlineSoft) else null,
        ) {
            CompositionLocalProvider(LocalHhButtonSurface provides HhButtonSurface.Tool) {
                if (stacked) {
                    FlowRow(
                        modifier = Modifier.padding(HhTheme.spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                        verticalArrangement = if (primaryLast) {
                            ReversedStack(HhTheme.spacing.sm)
                        } else {
                            Arrangement.spacedBy(HhTheme.spacing.sm)
                        },
                        maxItemsInEachRow = 1,
                        content = { actions() },
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(HhTheme.spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
                }
            }
        }
    }
}

private class ReversedStack(override val spacing: Dp) : Arrangement.Vertical {
    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        val gap = spacing.roundToPx()
        var position = 0
        for (index in sizes.indices.reversed()) {
            outPositions[index] = position
            position += sizes[index] + gap
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HhBottomActionBarPreview() {
    HhPreviewTheme(darkTheme = false) { HhBottomActionBarSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhBottomActionBarDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhBottomActionBarSample() }
}

@Composable
private fun HhBottomActionBarSample() {
    HhBottomActionBar(
        creditDisclosure = {
            Text(
                text = "Uses 1 of your 4 credits",
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
    ) {
        HhOutlinedButton(onClick = {}, modifier = Modifier.weight(1f), text = { Text("Preview") })
        HhButton(onClick = {}, modifier = Modifier.weight(1f), text = { Text("Export PDF") })
    }
}
