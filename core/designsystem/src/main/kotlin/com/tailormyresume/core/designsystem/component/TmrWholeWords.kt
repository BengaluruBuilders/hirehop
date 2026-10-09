package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

private const val SEARCH_STEP_SP = 0.25f

fun TextLayoutResult.splitsAWord(): Boolean {
    val text = layoutInput.text.text
    return (0 until lineCount - 1).any { line ->
        val end = getLineEnd(line)
        end in 1 until text.length && text[end - 1].isLetterOrDigit() && text[end].isLetterOrDigit()
    }
}

@Composable
fun TmrFitText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val density = LocalDensity.current
    val autoSize = remember(style.fontSize, density) {
        if (style.fontSize.isSpecified) {
            val base = style.fontSize
            val sameDpAsAtDefaultScale = with(density) { base.value.dp.toSp() }
            WholeWordsAutoSize(maxFontSize = base, minFontSize = sameDpAsAtDefaultScale.value.coerceAtMost(base.value).sp)
        } else {
            null
        }
    }
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        style = style,
        autoSize = autoSize,
        onTextLayout = onTextLayout,
    )
}

private data class WholeWordsAutoSize(val maxFontSize: TextUnit, val minFontSize: TextUnit) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit {
        if (!performLayout(constraints, text, maxFontSize).splitsAWord()) return maxFontSize
        if (performLayout(constraints, text, minFontSize).splitsAWord()) return minFontSize
        var fits = minFontSize.value
        var splits = maxFontSize.value
        while (splits - fits > SEARCH_STEP_SP) {
            val middle = (fits + splits) / 2
            if (performLayout(constraints, text, middle.sp).splitsAWord()) splits = middle else fits = middle
        }
        return fits.sp
    }
}
