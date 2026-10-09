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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrErrorCallout(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = TmrTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        shape = TmrTheme.shapes.banner,
        color = colors.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            Icon(
                imageVector = TmrIcons.Error,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(TmrSizeIconControl),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs)) {
                Text(text = title, style = TmrTheme.typography.titleS, color = colors.onSurface)
                if (supportingText != null) {
                    Text(text = supportingText, style = TmrTheme.typography.bodyM, color = colors.onSurface)
                }
                if (actionLabel != null && onAction != null) {
                    TmrOutlinedButton(onClick = onAction) {
                        Text(text = actionLabel, style = TmrTheme.typography.labelL, color = colors.onSurface)
                    }
                }
            }
        }
    }
}

private const val TMR_ERROR_CALLOUT_SAMPLE_TITLE = "That file did not import"
private const val TMR_ERROR_CALLOUT_SAMPLE_BODY = "The PDF has no readable text layer."
private const val TMR_ERROR_CALLOUT_SAMPLE_ACTION = "Use the guided form"

@Preview(showBackground = true)
@Composable
private fun TmrErrorCalloutPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrErrorCalloutSample() }
}

@Preview(showBackground = true)
@Composable
private fun TmrErrorCalloutDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrErrorCalloutSample() }
}

@Composable
private fun TmrErrorCalloutSample() {
    TmrErrorCallout(
        title = TMR_ERROR_CALLOUT_SAMPLE_TITLE,
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        supportingText = TMR_ERROR_CALLOUT_SAMPLE_BODY,
        actionLabel = TMR_ERROR_CALLOUT_SAMPLE_ACTION,
        onAction = {},
    )
}
