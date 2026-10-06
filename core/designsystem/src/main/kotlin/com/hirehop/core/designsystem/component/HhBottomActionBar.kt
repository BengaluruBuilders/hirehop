package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

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
    Column(modifier = modifier.fillMaxWidth().background(HhTheme.colors.background)) {
        HhDivider()
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(contentPadding ?: PaddingValues(horizontal = HhTheme.spacing.gutter))
                .padding(top = HhTheme.spacing.md, bottom = HhOuterGapBar),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (creditDisclosure != null) {
                creditDisclosure()
            }
            if (stacked) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(HhBarGap),
                    verticalArrangement = if (primaryLast) {
                        ReversedStack(HhBarGap)
                    } else {
                        Arrangement.spacedBy(HhBarGap)
                    },
                    maxItemsInEachRow = 1,
                    content = { actions() },
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhBarGap),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

@Composable
fun HhIconActionBar(
    secondaryIcon: ImageVector,
    secondaryContentDescription: String,
    onSecondaryClick: () -> Unit,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    primaryTrailingIcon: ImageVector? = null,
    secondaryBadge: String? = null,
    ink: Boolean = false,
) {
    HhBottomActionBar(modifier = modifier, stacked = false) {
        Box {
            HhIconButton(
                icon = secondaryIcon,
                contentDescription = secondaryContentDescription,
                onClick = onSecondaryClick,
                containerColor = HhTheme.colors.card,
                size = HhHeightButtonLarge,
            )
            if (secondaryBadge != null) {
                HhActionBadge(secondaryBadge, Modifier.align(Alignment.TopEnd))
            }
        }
        val primaryModifier = Modifier.weight(1f)
        if (ink) {
            HhInkButton(primaryLabel, onPrimaryClick, primaryModifier, primaryEnabled, trailingIcon = primaryTrailingIcon)
        } else {
            HhPrimaryButton(primaryLabel, onPrimaryClick, primaryModifier, primaryEnabled, trailingIcon = primaryTrailingIcon)
        }
    }
}

@Composable
private fun HhActionBadge(text: String, modifier: Modifier) {
    val colors = HhTheme.colors
    Box(
        modifier = modifier
            .offset(x = 2.dp, y = (-2).dp)
            .defaultMinSize(minWidth = HhBadgeSize, minHeight = HhBadgeSize)
            .background(colors.special, HhTheme.shapes.pill)
            .border(HhWidthStrokeFocus, colors.background, HhTheme.shapes.pill)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = colors.onSpecial,
        )
    }
}

private val HhBarGap = 10.dp
private val HhBadgeSize = 22.dp

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
