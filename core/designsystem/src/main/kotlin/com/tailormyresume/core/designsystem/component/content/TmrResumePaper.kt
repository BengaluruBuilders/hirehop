package com.tailormyresume.core.designsystem.component.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tailormyresume.core.designsystem.theme.TmrColors

enum class TmrPaperHighlight { None, FromResume, FromAnswer }

@Immutable
data class TmrPaperSpan(val text: String, val highlight: TmrPaperHighlight = TmrPaperHighlight.None)

@Immutable
data class TmrPaperBlock(
    val heading: String? = null,
    val title: String? = null,
    val dates: String? = null,
    val lines: List<List<TmrPaperSpan>> = emptyList(),
)

internal fun tmrPaperHighlightColor(highlight: TmrPaperHighlight, colors: TmrColors): Color = Color.Unspecified

@Composable
fun TmrResumePaper(
    name: String,
    contact: String,
    blocks: List<TmrPaperBlock>,
    coveragePercent: Int,
    modifier: Modifier = Modifier,
) {
}
