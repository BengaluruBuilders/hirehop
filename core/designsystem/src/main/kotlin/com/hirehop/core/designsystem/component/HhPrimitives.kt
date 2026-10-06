package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

internal enum class HhButtonSurface { Default, Header }

internal val LocalHhButtonSurface = staticCompositionLocalOf { HhButtonSurface.Default }

internal fun Modifier.hhFocusRing(focused: Boolean, color: Color, cornerRadius: Dp): Modifier {
    if (!focused) return this
    return drawBehind {
        val outset = 3.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(-outset, -outset),
            size = Size(size.width + outset * 2, size.height + outset * 2),
            cornerRadius = CornerRadius(cornerRadius.toPx() + outset),
            style = Stroke(width = HhWidthStrokeFocus.toPx()),
        )
    }
}

internal fun Modifier.hhDashedBorder(color: Color, width: Dp, cornerRadius: Dp): Modifier =
    drawBehind {
        val stroke = width.toPx()
        val inset = stroke / 2f
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 2.5f, stroke * 2f)),
            ),
        )
    }

@Composable
internal fun HhPill(
    container: Color,
    content: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    shape: Shape = HhTheme.shapes.pill,
    horizontalPadding: Dp = 12.dp,
    textStyle: TextStyle = HhTheme.typography.labelM,
    body: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = container,
        contentColor = content,
        border = border,
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            Row(
                modifier = Modifier
                    .heightIn(min = height)
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                androidx.compose.material3.ProvideTextStyle(textStyle) { body() }
            }
        }
    }
}
