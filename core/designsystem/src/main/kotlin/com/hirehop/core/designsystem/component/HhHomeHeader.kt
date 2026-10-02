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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhHomeHeader(
    greeting: String,
    headline: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
    action: (@Composable () -> Unit)? = null,
    illustration: (@Composable BoxScope.() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .layoutId(HhHeaderData(drawsAboveContent = true, overlap = HhOverlap.Sheet))
            .hhHeaderBackdrop(colors.header, colors.headerShape, statusTop + HOME_SMALL_CIRCLE_TOP, HhOverlap.Sheet)
            .heightIn(min = HhHeightHomeHeader + statusTop),
    ) {
        HhHomeHeaderText(statusTop, greeting, headline, trailing, action)
        if (illustration != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = statusTop + ILLUSTRATION_TOP)
                    .size(width = ILLUSTRATION_WIDTH, height = ILLUSTRATION_HEIGHT),
                content = illustration,
            )
        }
    }
}

@Composable
private fun HhHomeHeaderText(
    statusTop: androidx.compose.ui.unit.Dp,
    greeting: String,
    headline: String,
    trailing: @Composable RowScope.() -> Unit,
    action: (@Composable () -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = HhTheme.spacing.gutter, end = HhTheme.spacing.gutter, top = statusTop + 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = greeting,
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onHeader,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(HEADLINE_FRACTION)
                .padding(top = HhTheme.spacing.d24, bottom = HhOverlap.Sheet + HhTheme.spacing.d24),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d16),
        ) {
            Text(text = headline, style = HhTheme.typography.headlineL, color = HhTheme.colors.onHeader)
            if (action != null) {
                action()
            }
        }
    }
}

@Composable
fun HhCompactHomeHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = HhTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .layoutId(HhHeaderData(drawsAboveContent = false, overlap = HhOverlap.Sheet))
            .hhHeaderBackdrop(colors.header, colors.headerShape, statusTop + HOME_SMALL_CIRCLE_TOP, HhOverlap.Sheet)
            .heightIn(min = HhHeightCompactHomeHeader + statusTop)
            .padding(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = statusTop + HhTheme.spacing.lg,
                bottom = HhOverlap.Sheet + HhTheme.spacing.lg,
            ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.headlineL,
                color = colors.onHeader,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing,
            )
        }
        Text(text = subtitle, style = HhTheme.typography.bodyL, color = colors.onHeaderVariant)
    }
}

@Composable
fun HhCollapsedHomeHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = HhTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .layoutId(HhHeaderData(drawsAboveContent = false, overlap = HhOverlap.Sheet))
            .hhHeaderBackdrop(colors.header, colors.headerShape, statusTop + HOME_SMALL_CIRCLE_TOP, HhOverlap.Sheet)
            .heightIn(min = HhHeightCollapsedHomeHeader + statusTop)
            .padding(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = statusTop,
                bottom = HhOverlap.Sheet,
            ),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.titleL,
            color = colors.onHeader,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            content = trailing,
        )
    }
}

private val HOME_SMALL_CIRCLE_TOP = 170.dp
private val ILLUSTRATION_TOP = 86.dp
private val ILLUSTRATION_WIDTH = 174.dp
private val ILLUSTRATION_HEIGHT = 200.dp
private const val HEADLINE_FRACTION = 0.55f

@Preview(showBackground = true)
@Composable
private fun HhHomeHeaderPreview() {
    HhPreviewTheme(darkTheme = false) { HhHomeHeaderSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhHomeHeaderDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhHomeHeaderSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhCompactHomeHeaderPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhCompactHomeHeader(title = "Settings", subtitle = "priya@example.com", trailing = { HhCreditsPill(count = "4", label = "left") })
    }
}

@Preview(showBackground = true)
@Composable
private fun HhCollapsedHomeHeaderPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhCollapsedHomeHeader(title = "Applications", trailing = { HhCreditsPill(count = "4", label = "left") })
    }
}

@Composable
private fun HhHomeHeaderSample() {
    HhHomeHeader(
        greeting = "Hi, Priya",
        headline = "Your facts, every job",
        trailing = {
            HhCreditsPill(count = "4", label = "left")
            HhHeaderIconButton(icon = HhIcons.Bell, contentDescription = "Notifications", onClick = {})
        },
        action = { HhHeaderButton(label = "Paste a job", onClick = {}, trailingIcon = HhIcons.ArrowForward) },
    )
}
