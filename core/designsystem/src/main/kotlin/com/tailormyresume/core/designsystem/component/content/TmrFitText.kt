package com.tailormyresume.core.designsystem.component.content

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private const val ZERO_WIDTH_SPACE = '​'

private const val SHRINK_STEP = 0.92f

internal fun Modifier.readAs(text: String): Modifier = semantics { this.text = AnnotatedString(text) }

internal fun withBreakOpportunities(text: String, breakAfter: CharArray): String = buildString {
    text.forEach { char ->
        append(char)
        if (char in breakAfter) append(ZERO_WIDTH_SPACE)
    }
}

internal fun breaksInsideWord(layout: TextLayoutResult): Boolean {
    val text = layout.layoutInput.text.text
    return (0 until layout.lineCount - 1).any { line ->
        val end = layout.getLineEnd(line, visibleEnd = false)
        end in 1 until text.length && !text[end - 1].isBreakBoundary() && !text[end].isWhitespace()
    }
}

private fun Char.isBreakBoundary() = isWhitespace() || this == ZERO_WIDTH_SPACE || this == '-'

@Composable
internal fun TmrFitText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    breakAfter: CharArray = charArrayOf(),
) {
    val fontScale = LocalDensity.current.fontScale
    val floor: TextUnit = (style.fontSize.value / fontScale).sp
    var size by remember(text, style, fontScale) { mutableStateOf(style.fontSize) }
    Text(
        text = withBreakOpportunities(text, breakAfter),
        style = style,
        color = color,
        fontSize = size,
        modifier = modifier.readAs(text),
        onTextLayout = { layout ->
            if (breaksInsideWord(layout) && size.value > floor.value) {
                size = maxOf(size.value * SHRINK_STEP, floor.value).sp
            }
        },
    )
}
