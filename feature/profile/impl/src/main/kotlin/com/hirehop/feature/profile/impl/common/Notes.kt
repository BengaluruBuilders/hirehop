package com.hirehop.feature.profile.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

internal enum class NoteTone { Neutral, Positive, Warning, Plain }

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
    val stacked = LocalDensity.current.fontScale >= STACK_FONT_SCALE
    val container = when (tone) {
        NoteTone.Neutral -> colors.primaryContainer
        NoteTone.Positive -> colors.metContainer
        NoteTone.Warning -> colors.partialContainer
        NoteTone.Plain -> null
    }
    val content = when (tone) {
        NoteTone.Plain -> colors.onSurfaceVariant
        NoteTone.Warning -> colors.onPartialContainer
        else -> colors.onSurface
    }
    val frame = if (container != null) {
        modifier.fillMaxWidth().background(container, HhTheme.shapes.banner).padding(
            start = 16.dp,
            top = 14.dp,
            end = if (actionLabel != null) 8.dp else 16.dp,
            bottom = 14.dp,
        )
    } else {
        modifier.fillMaxWidth()
    }
    Column(modifier = frame, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (container != null) 12.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(if (container != null) 22.dp else 18.dp),
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
                color = content,
            )
            if (actionLabel != null && onAction != null && !stacked) {
                HhOutlineButton(label = actionLabel, onClick = onAction, size = HhButtonSize.Compact)
            }
        }
        if (actionLabel != null && onAction != null && stacked) {
            HhOutlineButton(label = actionLabel, onClick = onAction, size = HhButtonSize.Compact)
        }
    }
}

private const val STACK_FONT_SCALE = 1.5f
