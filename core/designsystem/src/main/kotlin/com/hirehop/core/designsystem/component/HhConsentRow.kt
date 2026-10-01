package com.hirehop.core.designsystem.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhConsentRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    HhListRow(
        modifier = modifier,
        trailing = { HhConsentRowSwitch(checked = checked, onCheckedChange = onCheckedChange) },
    ) {
        Text(
            text = label,
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HhConsentRowSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = HhTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            uncheckedThumbColor = colors.outline,
            uncheckedTrackColor = colors.surfaceContainerHighest,
            uncheckedBorderColor = colors.hairlineStrong,
        ),
    )
}

private const val HH_CONSENT_ROW_SAMPLE_LABEL = "Send anonymous usage counts"
private const val HH_CONSENT_ROW_SAMPLE_BODY = "Optional. Never your text."

@Preview(showBackground = true)
@Composable
private fun HhConsentRowPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhConsentRow(
            label = HH_CONSENT_ROW_SAMPLE_LABEL,
            supportingText = HH_CONSENT_ROW_SAMPLE_BODY,
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhConsentRowDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhConsentRow(
            label = HH_CONSENT_ROW_SAMPLE_LABEL,
            supportingText = HH_CONSENT_ROW_SAMPLE_BODY,
            checked = true,
            onCheckedChange = {},
        )
    }
}
