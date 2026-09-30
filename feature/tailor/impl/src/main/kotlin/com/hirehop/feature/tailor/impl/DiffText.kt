package com.hirehop.feature.tailor.impl

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.hirehop.feature.tailor.impl.diff.DiffSegment

@Composable
internal fun DiffText(
    segments: List<DiffSegment>,
    highlight: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val text = remember(segments, highlight) {
        buildAnnotatedString {
            segments.forEachIndexed { index, segment ->
                if (index > 0) append(" ")
                if (segment.changed) {
                    withStyle(SpanStyle(background = highlight, fontWeight = FontWeight.SemiBold)) {
                        append(segment.text)
                    }
                } else {
                    append(segment.text)
                }
            }
        }
    }
    Text(text = text, style = style, modifier = modifier)
}
