package com.hirehop.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.em
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HhTheme.colors.onSurfaceVariant,
) {
    Text(
        text = text.toUpperCase(Locale.current),
        modifier = modifier.semantics {
            heading()
            contentDescription = text
        },
        style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.06.em),
        color = color,
    )
}
