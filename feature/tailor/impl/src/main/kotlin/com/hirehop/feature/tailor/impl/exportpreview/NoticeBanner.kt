package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhTheme

internal enum class NoticeTone(val icon: ImageVector) {
    Quiet(HhIcons.Info),
    Raised(HhIcons.Info),
    Good(HhIcons.CheckCircle),
    Warning(HhIcons.Clock),
    Error(HhIcons.Error),
    Offline(HhIcons.Offline),
}

private fun NoticeTone.fill(colors: HhColors): Color = when (this) {
    NoticeTone.Quiet -> colors.card
    NoticeTone.Raised, NoticeTone.Offline -> colors.primaryContainer
    NoticeTone.Good -> colors.metContainer
    NoticeTone.Warning -> colors.partialContainer
    NoticeTone.Error -> colors.errorContainer
}

private fun NoticeTone.iconTint(colors: HhColors): Color = when (this) {
    NoticeTone.Good -> colors.met
    NoticeTone.Warning -> colors.partial
    NoticeTone.Error -> colors.error
    else -> colors.onSurfaceVariant
}

@Composable
internal fun NoticeBanner(
    message: String,
    tone: NoticeTone,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(tone.fill(colors), HhTheme.shapes.banner)
            .padding(horizontal = HhTheme.spacing.lg, vertical = HhTheme.spacing.d12 + HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = tone.icon,
            contentDescription = null,
            tint = tone.iconTint(colors),
            modifier = Modifier.size(HhTheme.spacing.xl),
        )
        Text(text = message, style = HhTheme.typography.titleS, color = colors.onSurface)
    }
}
