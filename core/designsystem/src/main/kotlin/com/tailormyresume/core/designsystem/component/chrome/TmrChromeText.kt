package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun TmrCapsText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val wrap = LocalDensity.current.fontScale > 1f
    Text(
        text = text.uppercase(),
        style = style,
        color = color,
        softWrap = wrap,
        maxLines = if (wrap) 2 else 1,
        overflow = overflow,
        modifier = modifier.clearAndSetSemantics { this.text = AnnotatedString(text) },
    )
}
