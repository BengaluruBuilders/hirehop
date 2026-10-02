package com.hirehop.feature.settings.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun SettingsNeutralCallout(
    text: String,
    modifier: Modifier = Modifier,
) {
    val shape = HhTheme.shapes.banner
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive }
            .clip(shape)
            .background(HhTheme.colors.card)
            .border(width = CALLOUT_BORDER, color = HhTheme.colors.outline, shape = shape)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Icon(
            imageVector = HhIcons.Error,
            contentDescription = null,
            tint = HhTheme.colors.onSurface,
            modifier = Modifier.size(CALLOUT_ICON_SIZE),
        )
        Text(
            text = text,
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private val CALLOUT_BORDER = 1.5.dp
private val CALLOUT_ICON_SIZE = 20.dp
