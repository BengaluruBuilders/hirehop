package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

val LocalHhBottomInset = compositionLocalOf { 0.dp }

private enum class HhScreenSlot { Header, Sheet, Content, Bottom, Notice, Snackbar, Action }

@Composable
fun HhScreen(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    sheet: Boolean = true,
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    floatingAction: (@Composable () -> Unit)? = null,
    bottomBarNotice: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = HhTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val resting = navBottom + LocalHhBottomInset.current
    val gutter = HhTheme.spacing.gutter
    val sheetTop = HhTheme.spacing.d24
    SubcomposeLayout(
        modifier = modifier
            .fillMaxSize()
            .background(if (sheet) colors.background else colors.ground),
    ) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val loose = Constraints(maxWidth = width, maxHeight = height)
        val headerItem = header?.let { subcompose(HhScreenSlot.Header, it).firstOrNull() }
        val headerPlaceable = headerItem?.measure(Constraints(minWidth = width, maxWidth = width, maxHeight = height))
        val data = headerItem?.layoutId as? HhHeaderData
        val bottoms = bottomBar?.let { subcompose(HhScreenSlot.Bottom, it) }.orEmpty().map { it.measure(loose) }
        val barHeight = bottoms.maxOfOrNull { it.height } ?: 0
        val notices = bottomBarNotice?.let {
            subcompose(HhScreenSlot.Notice) { HhBarNotice(it) }
        }.orEmpty().map { it.measure(loose) }
        val noticeHeight = notices.maxOfOrNull { it.height } ?: 0
        val floor = if (barHeight > 0) barHeight else resting.roundToPx()
        val reserved = if (barHeight > 0 || noticeHeight > 0) floor + noticeHeight else 0
        val contentTop = if (headerPlaceable == null) {
            0
        } else {
            (headerPlaceable.height - (data?.overlap ?: 0.dp).roundToPx()).coerceAtLeast(0)
        }
        val padding = PaddingValues(
            top = if (headerPlaceable == null) {
                statusTop
            } else if (sheet) {
                sheetTop
            } else {
                0.dp
            },
            bottom = if (reserved > 0) gutter else resting,
        )
        val fullArea = Constraints.fixed(width, (height - contentTop).coerceAtLeast(0))
        val area = Constraints.fixed(width, (height - contentTop - reserved).coerceAtLeast(0))
        val sheetItems = if (sheet && headerPlaceable != null) {
            subcompose(HhScreenSlot.Sheet) { HhSheet(Modifier.fillMaxSize()) {} }.map { it.measure(fullArea) }
        } else {
            emptyList()
        }
        val contents = subcompose(HhScreenSlot.Content) { content(padding) }.map { it.measure(area) }
        val snacks = subcompose(HhScreenSlot.Snackbar, snackbarHost).map { it.measure(loose) }
        val actions = floatingAction?.let { subcompose(HhScreenSlot.Action, it) }.orEmpty().map { it.measure(loose) }
        layout(width, height) {
            val aboveContent = data?.drawsAboveContent == true
            if (!aboveContent) headerPlaceable?.place(0, 0)
            sheetItems.forEach { it.place(0, contentTop) }
            contents.forEach { it.place(0, contentTop) }
            if (aboveContent) headerPlaceable?.place(0, 0)
            bottoms.forEach { it.place((width - it.width) / 2, height - it.height) }
            notices.forEach { it.place(0, height - floor - it.height) }
            val lift = floor + noticeHeight
            snacks.forEach { it.place((width - it.width) / 2, height - lift - it.height) }
            actions.forEach { it.place(width - it.width, height - lift - it.height - snacks.heightSum()) }
        }
    }
}

@Composable
private fun HhBarNotice(notice: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = HhTheme.spacing.gutter).padding(bottom = HhTheme.spacing.sm)) {
        notice()
    }
}

private fun List<Placeable>.heightSum(): Int = sumOf { it.height }

@Preview(showBackground = true)
@Composable
private fun HhScreenPreview() {
    HhPreviewTheme(darkTheme = false) { HhScreenSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhScreenDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhScreenSample() }
}

@Composable
private fun HhScreenSample() {
    HhScreen(
        header = { HhInnerHeader(title = "Applications", onBack = {}, extended = false) },
        bottomBar = { HhBottomActionBar { HhPrimaryButton(label = "Export", onClick = {}) } },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}
