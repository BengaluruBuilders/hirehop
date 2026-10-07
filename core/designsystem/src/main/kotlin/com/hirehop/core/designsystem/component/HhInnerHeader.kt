package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhInnerHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "",
    backIcon: ImageVector = HhIcons.Back,
    trailing: (@Composable () -> Unit)? = null,
    belowTitle: (@Composable () -> Unit)? = null,
    extended: Boolean = true,
    overlap: Dp = if (extended) HhOverlap.HeroCard else HhOverlap.Sheet,
) {
    val colors = HhTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minHeight = if (extended) HhHeightInnerHeader else HhHeightInnerHeaderCompact
    Box(
        modifier = modifier
            .fillMaxWidth()
            .layoutId(HhHeaderData(drawsAboveContent = false, overlap = overlap))
            .hhHeaderBackdrop(colors.header, null, if (extended) 0.dp else HhHeroBottomRadius)
            .heightIn(min = minHeight + statusTop),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = HhTheme.spacing.gutter,
                    end = HhTheme.spacing.gutter,
                    top = statusTop + HhTheme.spacing.gutter,
                    bottom = if (extended) overlap else overlap + HhTheme.spacing.sm,
                ),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            if (onBack != null) {
                HhHeaderIconButton(icon = backIcon, contentDescription = backContentDescription, onClick = onBack)
            }
            HhInnerHeaderTitle(title, subtitle, belowTitle, Modifier.weight(1f))
            Box(modifier = Modifier.defaultMinSize(minWidth = HhHeightTouch), contentAlignment = Alignment.CenterEnd) {
                if (trailing != null) {
                    trailing()
                }
            }
        }
    }
}

@Composable
private fun HhInnerHeaderTitle(
    title: String,
    subtitle: String?,
    belowTitle: (@Composable () -> Unit)?,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.padding(top = 2.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onHeader,
            textAlign = TextAlign.Start,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onHeaderVariant,
                textAlign = TextAlign.Start,
            )
        }
        if (belowTitle != null) {
            Box(modifier = Modifier.padding(top = HhTheme.spacing.sm)) { belowTitle() }
        }
    }
}

private val HhHeroBottomRadius = 28.dp

@Preview(showBackground = true)
@Composable
private fun HhInnerHeaderPreview() {
    HhPreviewTheme(darkTheme = false) { HhInnerHeaderSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhInnerHeaderDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhInnerHeaderSample() }
}

@Composable
private fun HhInnerHeaderSample() {
    HhInnerHeader(
        title = "Fit for Kestrel Labs",
        subtitle = "Android Developer · 1 to 2 years",
        onBack = {},
        backContentDescription = "Back",
        belowTitle = { HhPageDots(count = 3, selectedIndex = 0) },
    )
}
