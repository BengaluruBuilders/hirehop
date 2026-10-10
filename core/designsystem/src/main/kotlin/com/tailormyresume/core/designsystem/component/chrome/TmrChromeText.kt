package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density

private const val MAX_CHROME_FONT_SCALE = 1.3f

@Composable
internal fun TmrCapsText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(MAX_CHROME_FONT_SCALE))) {
        Text(
            text = text.uppercase(),
            style = style,
            color = color,
            maxLines = 1,
            softWrap = false,
            modifier = modifier.clearAndSetSemantics { this.text = AnnotatedString(text) },
        )
    }
}
