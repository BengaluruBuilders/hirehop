package com.hirehop.core.designsystem.component

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhOfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    visible: Boolean = true,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    if (!visible) return
    val colors = HhTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HhTheme.shapes.banner,
        color = colors.neutralContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = HhIcons.Offline,
                contentDescription = null,
                tint = colors.onNeutralContainer,
                modifier = Modifier.size(HhSizeIcon),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = message, style = HhTheme.typography.bodyM, color = colors.onSurface)
                if (supportingText != null) {
                    Text(text = supportingText, style = HhTheme.typography.bodyS, color = colors.onSurfaceVariant)
                }
            }
            if (actionLabel != null && onAction != null) {
                HhTextButton(label = actionLabel, onClick = onAction)
            }
        }
    }
}

private const val HH_OFFLINE_BANNER_SAMPLE_TITLE = "You are offline. Saved work still opens."

@Preview(showBackground = true)
@Composable
private fun HhOfflineBannerPreview() {
    HhPreviewTheme(darkTheme = false) { HhOfflineBannerSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhOfflineBannerDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhOfflineBannerSample() }
}

@Composable
private fun HhOfflineBannerSample() {
    HhOfflineBanner(
        message = HH_OFFLINE_BANNER_SAMPLE_TITLE,
        modifier = Modifier.padding(HhTheme.spacing.lg),
        actionLabel = "Retry",
        onAction = {},
    )
}
