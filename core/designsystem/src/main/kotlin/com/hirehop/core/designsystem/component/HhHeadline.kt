package com.hirehop.core.designsystem.component

import androidx.compose.material3.Text
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
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhHeadline(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = HhTheme.typography.displayM,
    color: Color = HhTheme.colors.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
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
