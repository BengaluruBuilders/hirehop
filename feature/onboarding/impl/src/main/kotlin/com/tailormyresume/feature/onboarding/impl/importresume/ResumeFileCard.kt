package com.tailormyresume.feature.onboarding.impl.importresume

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

private const val ZERO_WIDTH_SPACE = '​'
private const val AMBER_TILE_ALPHA = 0.12f
private val NAME_SEPARATORS = charArrayOf('_', '.', '-', '/')

private fun String.breakableAfterSeparators(): String = buildString {
    for (character in this@breakableAfterSeparators) {
        append(character)
        if (character in NAME_SEPARATORS) append(ZERO_WIDTH_SPACE)
    }
}

@Composable
internal fun ResumeFileCard(
    name: String?,
    meta: String,
    modifier: Modifier = Modifier,
    amber: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val colors = TmrTheme.colors
    val ink = colors.ink
    val titleColor = if (amber) ink else colors.text
    val metaColor = if (amber) ink else colors.textMuted
    val tile = if (amber) ink.copy(alpha = AMBER_TILE_ALPHA) else colors.fill
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (amber) colors.amber else colors.surfaceRaised, TmrTheme.shapes.card)
            .padding(TmrTheme.spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        if (name != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(tile, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = TmrIcons.File,
                        contentDescription = null,
                        tint = titleColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = name.breakableAfterSeparators(),
                        style = TmrTheme.typography.strongLarge,
                        color = titleColor,
                        modifier = Modifier.semantics { text = AnnotatedString(name) },
                    )
                    if (meta.isNotBlank()) {
                        Text(text = meta, style = TmrTheme.typography.caption, color = metaColor)
                    }
                }
            }
        }
        content()
    }
}
