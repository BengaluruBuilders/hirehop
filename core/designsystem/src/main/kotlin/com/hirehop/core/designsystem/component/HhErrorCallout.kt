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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhErrorCallout(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HhTheme.shapes.banner,
        color = colors.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(HhTheme.spacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Icon(
                imageVector = HhIcons.Error,
                contentDescription = null,
                tint = colors.onErrorContainer,
                modifier = Modifier.padding(top = HhTheme.spacing.xxs).size(HhSizeIcon),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
                Text(text = title, style = HhTheme.typography.titleS, color = colors.onErrorContainer)
                if (supportingText != null) {
                    Text(text = supportingText, style = HhTheme.typography.bodyM, color = colors.onErrorContainer)
                }
                if (actionLabel != null && onAction != null) {
                    HhOutlinedButton(onClick = onAction) {
                        Text(text = actionLabel, style = HhTheme.typography.labelL, color = colors.onErrorContainer)
                    }
                }
            }
        }
    }
}

private const val HH_ERROR_CALLOUT_SAMPLE_TITLE = "That file did not import"
private const val HH_ERROR_CALLOUT_SAMPLE_BODY = "The PDF has no readable text layer."
private const val HH_ERROR_CALLOUT_SAMPLE_ACTION = "Use the guided form"

@Preview(showBackground = true)
@Composable
private fun HhErrorCalloutPreview() {
    HhPreviewTheme(darkTheme = false) { HhErrorCalloutSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhErrorCalloutDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhErrorCalloutSample() }
}

@Composable
private fun HhErrorCalloutSample() {
    HhErrorCallout(
        title = HH_ERROR_CALLOUT_SAMPLE_TITLE,
        modifier = Modifier.padding(HhTheme.spacing.lg),
        supportingText = HH_ERROR_CALLOUT_SAMPLE_BODY,
        actionLabel = HH_ERROR_CALLOUT_SAMPLE_ACTION,
        onAction = {},
    )
}
