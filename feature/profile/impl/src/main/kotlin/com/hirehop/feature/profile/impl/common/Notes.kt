package com.hirehop.feature.profile.impl.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

private val NoteIconSize = 20.dp
private val NoteBorderWidth = 1.dp

internal enum class NoteTone { Neutral, Positive, Outlined }

@Composable
internal fun Note(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoteTone = NoteTone.Neutral,
    icon: ImageVector = HhIcons.Error,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    val container: Color
    val content: Color
    val border: BorderStroke?
    val shape = if (tone == NoteTone.Outlined) HhTheme.shapes.card else HhTheme.shapes.banner
    when (tone) {
        NoteTone.Neutral -> {
            container = colors.neutralContainer
            content = colors.onNeutralContainer
            border = null
        }

        NoteTone.Positive -> {
            container = colors.metContainer
            content = colors.onMetContainer
            border = null
        }

        NoteTone.Outlined -> {
            container = colors.card
            content = colors.onSurface
            border = BorderStroke(NoteBorderWidth, colors.outlineVariant)
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = container,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(
                start = HhTheme.spacing.cardPadding,
                top = HhTheme.spacing.md,
                end = if (actionLabel != null) HhTheme.spacing.sm else HhTheme.spacing.cardPadding,
                bottom = HhTheme.spacing.md,
            ),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(NoteIconSize),
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.bodyM,
                color = content,
            )
            if (actionLabel != null && onAction != null) {
                HhTextButton(label = actionLabel, onClick = onAction)
            }
        }
    }
}
