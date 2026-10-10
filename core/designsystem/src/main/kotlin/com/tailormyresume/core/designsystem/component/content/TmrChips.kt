package com.tailormyresume.core.designsystem.component.content

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tailormyresume.core.designsystem.R
import com.tailormyresume.core.designsystem.theme.TmrColors

enum class TmrKeywordState { Have, Missing }

enum class TmrSource(@StringRes val labelRes: Int) {
    YourResume(R.string.content_source_your_resume),
    YourAnswer(R.string.content_source_your_answer),
}

enum class TmrTag(@StringRes val labelRes: Int) {
    Soon(R.string.content_tag_soon),
    Add(R.string.content_tag_add),
    BestValue(R.string.content_tag_best_value),
    Required(R.string.content_tag_required),
    New(R.string.content_tag_new),
    Rewritten(R.string.content_tag_rewritten),
    Added(R.string.content_tag_added),
    Reordered(R.string.content_tag_reordered),
}

@Immutable
data class TmrChipStyle(val text: Color, val fill: Color, val border: Color)

internal fun tmrKeywordChipStyle(state: TmrKeywordState, colors: TmrColors): TmrChipStyle =
    TmrChipStyle(Color.Unspecified, Color.Unspecified, Color.Unspecified)

internal fun tmrSourceColor(source: TmrSource, colors: TmrColors): Color = Color.Unspecified

internal fun tmrTagStyle(tag: TmrTag, colors: TmrColors): TmrChipStyle =
    TmrChipStyle(Color.Unspecified, Color.Unspecified, Color.Unspecified)

@Composable
fun TmrKeywordChip(label: String, state: TmrKeywordState, modifier: Modifier = Modifier) {
}

@Composable
fun TmrSourceChip(source: TmrSource, modifier: Modifier = Modifier) {
}

@Composable
fun TmrTagChip(tag: TmrTag, modifier: Modifier = Modifier) {
}
