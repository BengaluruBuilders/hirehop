package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.getTextLayoutResult
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

private const val SHRINK_STEP = 0.9f

private fun TextLayoutResult.splitsWord(): Boolean {
    val source = layoutInput.text.text
    return (0 until lineCount - 1).any { line ->
        val end = getLineEnd(line)
        !source[end - 1].isWhitespace() && !source[end].isWhitespace()
    }
}

@Composable
internal fun TmrCapsText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale
    val enlarged = fontScale > 1f
    val oneXSize = (style.fontSize.value / fontScale).sp
    var size by remember(text, style.fontSize, fontScale) { mutableStateOf(style.fontSize) }
    val layout = remember { arrayOfNulls<TextLayoutResult>(1) }
    Text(
        text = text.uppercase(),
        style = style.copy(fontSize = size),
        color = color,
        softWrap = enlarged,
        maxLines = if (enlarged) Int.MAX_VALUE else 1,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            layout[0] = result
            if (enlarged && result.splitsWord() && size > oneXSize) {
                size = (size.value * SHRINK_STEP).sp.let { if (it < oneXSize) oneXSize else it }
            }
        },
        modifier =
        modifier.clearAndSetSemantics {
            this.text = AnnotatedString(text)
            getTextLayoutResult { layout[0]?.let(it::add) != null }
        },
    )
}
