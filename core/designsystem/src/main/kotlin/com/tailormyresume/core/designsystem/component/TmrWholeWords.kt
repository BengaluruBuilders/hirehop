package com.tailormyresume.core.designsystem.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

private const val MIN_SHRINK_STEP_SP = 0.25f
private const val FIT_MARGIN = 0.98f
const val TMR_BRAND_NAME = "TailorMyResume"

private fun Char.continuesWord(): Boolean = !isWhitespace() && this != '-' && this != '\u00AD'

fun TextLayoutResult.splitsAWord(): Boolean {
    val text = layoutInput.text.text
    return (0 until lineCount - 1).any { line ->
        val end = getLineEnd(line)
        end in 1 until text.length && text[end - 1].continuesWord() && text[end].continuesWord()
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
    minScale: Float? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    TmrFitText(AnnotatedString(text), modifier, style, color, textAlign, maxLines, overflow, minScale, lineBreak, onTextLayout)
}

@Composable
fun TmrFitText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    minScale: Float? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val density = LocalDensity.current
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val baseSp = style.fontSize.takeIf { it.isSpecified }?.value
    val floorSp = baseSp?.let { base ->
        if (minScale != null) base * minScale else with(density) { base.dp.toSp() }.value.coerceAtMost(base)
    }
    var sizeSp by remember(text, baseSp, minScale, density, windowWidth) { mutableFloatStateOf(baseSp ?: 0f) }
    var splitsAtFloor by remember(text, baseSp, minScale, density, windowWidth) { mutableStateOf(false) }
    val mayHyphenate = minScale == null && !text.text.contains(TMR_BRAND_NAME)
    val fitStyle = style.copy(
        fontSize = if (baseSp != null) sizeSp.sp else style.fontSize,
        hyphens = if (mayHyphenate && splitsAtFloor) Hyphens.Auto else Hyphens.None,
        lineBreak = lineBreak,
    )
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        style = fitStyle,
        onTextLayout = { layout ->
            if (floorSp != null && layout.splitsAWord()) {
                if (sizeSp > floorSp + MIN_SHRINK_STEP_SP) {
                    sizeSp = layout.fittedSizeSp(sizeSp).coerceIn(floorSp, sizeSp - MIN_SHRINK_STEP_SP)
                } else if (mayHyphenate && !splitsAtFloor) {
                    splitsAtFloor = true
                }
            }
            onTextLayout(layout)
        },
    )
}

private fun TextLayoutResult.fittedSizeSp(currentSp: Float): Float {
    val longestWord = multiParagraph.minIntrinsicWidth
    val available = layoutInput.constraints.maxWidth
    return if (longestWord > 0f) currentSp * available / longestWord * FIT_MARGIN else currentSp
}
