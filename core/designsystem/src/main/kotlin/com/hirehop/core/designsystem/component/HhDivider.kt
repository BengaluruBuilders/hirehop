package com.hirehop.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhDividerStyle { Hairline, Dashed }

@Composable
fun HhDivider(
    modifier: Modifier = Modifier,
    style: HhDividerStyle = HhDividerStyle.Hairline,
    thickness: Dp = HhWidthHairline,
    color: Color? = null,
) {
    val lineColor = color ?: HhTheme.colors.hairline
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness),
    ) {
        val y = size.height / 2f
        val dash = size.height * 4f
        val effect = if (style == HhDividerStyle.Dashed) {
            PathEffect.dashPathEffect(intervals = floatArrayOf(dash, dash))
        } else {
            null
        }
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = size.height,
            pathEffect = effect,
        )
    }
}

private const val HH_DIVIDER_SAMPLE_TITLE = "Original"
private const val HH_DIVIDER_SAMPLE_BODY = "Wrote weekly SQL reports in PostgreSQL."

@Preview(showBackground = true)
@Composable
private fun HhDividerPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhDividerPreviewColumn()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhDividerDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhDividerPreviewColumn()
    }
}

@Composable
private fun HhDividerPreviewColumn() {
    Column(modifier = Modifier.padding(HhTheme.spacing.lg)) {
        Text(text = HH_DIVIDER_SAMPLE_TITLE, style = HhTheme.typography.labelMedium)
        HhDivider()
        Text(text = HH_DIVIDER_SAMPLE_BODY, style = HhTheme.typography.bodyLarge)
        HhDivider(style = HhDividerStyle.Dashed)
    }
}
