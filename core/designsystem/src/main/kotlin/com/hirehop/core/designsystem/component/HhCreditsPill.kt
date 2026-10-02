package com.hirehop.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhCreditsPill(
    count: String,
    label: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = HhTheme.colors
    val semanticsModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics { this.contentDescription = contentDescription }
    }
    HhPill(
        container = colors.onHeader,
        content = colors.onHeaderControl,
        height = HhHeightChipLarge,
        modifier = semanticsModifier,
        horizontalPadding = 14.dp,
        textStyle = HhTheme.typography.labelL,
    ) {
        Text(
            text = count,
            style = HhTheme.typography.numeralM.copy(fontSize = HhTheme.typography.labelL.fontSize),
            color = colors.onHeaderControl,
        )
        Text(text = label, style = HhTheme.typography.labelL, color = colors.onHeaderControl)
    }
}

@Preview
@Composable
private fun HhCreditsPillPreview() {
    HhPreviewTheme(darkTheme = false) { HhCreditsPill(count = "4", label = "left") }
}

@Preview
@Composable
private fun HhCreditsPillDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhCreditsPill(count = "4", label = "left") }
}
