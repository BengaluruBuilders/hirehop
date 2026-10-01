package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhOfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    visible: Boolean = true,
) {
    if (!visible) return
    val colors = HhTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HhTheme.shapes.sm),
        color = colors.spotContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = HhTheme.spacing.lg,
                vertical = HhTheme.spacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = colors.onSpotContainer,
                modifier = Modifier.size(HhSizeIcon),
            )
            Spacer(Modifier.width(HhTheme.spacing.sm))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = message,
                    style = HhTheme.typography.labelMedium,
                    color = colors.onSpotContainer,
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = HhTheme.typography.bodySmall,
                        color = colors.onSpotContainer,
                    )
                }
            }
        }
    }
}

private const val HH_OFFLINE_BANNER_SAMPLE_TITLE = "Saved on this phone"
private const val HH_OFFLINE_BANNER_SAMPLE_BODY = "HireHop sends nothing until you choose to."

@Preview(showBackground = true)
@Composable
private fun HhOfflineBannerPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhOfflineBanner(
            message = HH_OFFLINE_BANNER_SAMPLE_TITLE,
            supportingText = HH_OFFLINE_BANNER_SAMPLE_BODY,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhOfflineBannerDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhOfflineBanner(
            message = HH_OFFLINE_BANNER_SAMPLE_TITLE,
            supportingText = HH_OFFLINE_BANNER_SAMPLE_BODY,
        )
    }
}
