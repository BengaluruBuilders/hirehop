package com.tailormyresume.feature.tailor.impl.exportpreview

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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrColors
import com.tailormyresume.core.designsystem.theme.TmrTheme

internal enum class NoticeTone(val icon: ImageVector) {
    Quiet(TmrIcons.Info),
    Raised(TmrIcons.Info),
    Good(TmrIcons.CheckCircle),
    Warning(TmrIcons.Clock),
    Error(TmrIcons.Error),
    Offline(TmrIcons.Offline),
}

private val NoticeTone.announces: Boolean get() = this == NoticeTone.Error || this == NoticeTone.Offline

private fun NoticeTone.fill(colors: TmrColors): Color = when (this) {
    NoticeTone.Quiet -> colors.card
    NoticeTone.Raised, NoticeTone.Offline -> colors.primaryContainer
    NoticeTone.Good -> colors.metContainer
    NoticeTone.Warning -> colors.partialContainer
    NoticeTone.Error -> colors.errorContainer
}

private fun NoticeTone.iconTint(colors: TmrColors): Color = when (this) {
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
    val colors = TmrTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { if (tone.announces) liveRegion = LiveRegionMode.Polite }
            .background(tone.fill(colors), TmrTheme.shapes.banner)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = TmrTheme.spacing.d12 + TmrTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = tone.icon,
            contentDescription = null,
            tint = tone.iconTint(colors),
            modifier = Modifier.size(TmrTheme.spacing.xl),
        )
        Text(text = message, style = TmrTheme.typography.titleS, color = colors.onSurface)
    }
}
