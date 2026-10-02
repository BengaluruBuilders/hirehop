package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhConsentRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    linkLabel: String? = null,
    onLinkClick: (() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HhTheme.shapes.card,
        color = colors.card,
        border = BorderStroke(HhWidthHairline, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
                .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            HhCheckbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
                Text(
                    text = label,
                    modifier = Modifier.padding(top = HhTheme.spacing.sm),
                    style = HhTheme.typography.bodyM,
                    color = colors.onSurface,
                )
                if (supportingText != null) {
                    Text(text = supportingText, style = HhTheme.typography.bodyS, color = colors.onSurfaceVariant)
                }
                if (linkLabel != null && onLinkClick != null) {
                    HhTextButton(label = linkLabel, onClick = onLinkClick)
                }
            }
        }
    }
}

private const val HH_CONSENT_ROW_SAMPLE_LABEL = "Use my resume to tailor applications. It is never shared with employers."

@Preview(showBackground = true)
@Composable
private fun HhConsentRowPreview() {
    HhPreviewTheme(darkTheme = false) { HhConsentRowSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhConsentRowDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhConsentRowSample() }
}

@Composable
private fun HhConsentRowSample() {
    HhConsentRow(
        label = HH_CONSENT_ROW_SAMPLE_LABEL,
        checked = true,
        onCheckedChange = {},
        modifier = Modifier.padding(HhTheme.spacing.lg),
        linkLabel = "How we use your data",
        onLinkClick = {},
    )
}
