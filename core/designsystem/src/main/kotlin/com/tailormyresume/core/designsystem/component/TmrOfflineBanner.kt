package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrOfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    visible: Boolean = true,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    TmrVisibility(visible = visible, modifier = modifier, rise = true) {
        TmrOfflineBannerBody(message, supportingText, actionLabel, onAction)
    }
}

@Composable
private fun TmrOfflineBannerBody(
    message: String,
    supportingText: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    val colors = TmrTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        shape = TmrTheme.shapes.banner,
        color = colors.neutralContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            Icon(
                imageVector = TmrIcons.Offline,
                contentDescription = null,
                tint = colors.onSurface,
                modifier = Modifier.size(TmrSizeIconControl),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = message, style = TmrTheme.typography.titleS, color = colors.onSurface)
                if (supportingText != null) {
                    Text(text = supportingText, style = TmrTheme.typography.bodyS, color = colors.onSurfaceVariant)
                }
            }
            if (actionLabel != null && onAction != null) {
                TmrTextButton(label = actionLabel, onClick = onAction)
            }
        }
    }
}

private const val TMR_OFFLINE_BANNER_SAMPLE_TITLE = "You are offline. Saved work still opens."

@Preview(showBackground = true)
@Composable
private fun TmrOfflineBannerPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrOfflineBannerSample() }
}

@Preview(showBackground = true)
@Composable
private fun TmrOfflineBannerDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrOfflineBannerSample() }
}

@Composable
private fun TmrOfflineBannerSample() {
    TmrOfflineBanner(
        message = TMR_OFFLINE_BANNER_SAMPLE_TITLE,
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        actionLabel = "Retry",
        onAction = {},
    )
}
