package com.hirehop.feature.tailor.impl

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.hirehop.feature.tailor.impl.diff.DiffSegment
import com.hirehop.feature.tailor.impl.diff.joinedText

@Composable
internal fun DiffText(
    segments: List<DiffSegment>,
    highlight: Color,
    changeDecoration: TextDecoration,
    @StringRes changedWordsRes: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val text = remember(segments, highlight, changeDecoration) {
        buildAnnotatedString {
            segments.forEachIndexed { index, segment ->
                if (index > 0) append(" ")
                if (segment.changed) {
                    withStyle(
                        SpanStyle(
                            background = highlight,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = changeDecoration,
                        ),
                    ) { append(segment.text) }
                } else {
                    append(segment.text)
                }
            }
        }
    }
    val changedWords = segments.filter { it.changed }.joinToString(", ") { it.text }
    val description = if (changedWords.isEmpty()) {
        segments.joinedText()
    } else {
        "${segments.joinedText()}. ${stringResource(changedWordsRes, changedWords)}"
    }
    Text(
        text = text,
        style = style,
        modifier = modifier.semantics { contentDescription = description },
    )
}
