package com.tailormyresume.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrHeadline(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TmrTheme.typography.displayM,
    color: Color = TmrTheme.colors.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    TmrFitText(
        text = text.toUpperCase(Locale.current),
        modifier = modifier.semantics {
            heading()
            contentDescription = text
        },
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}
