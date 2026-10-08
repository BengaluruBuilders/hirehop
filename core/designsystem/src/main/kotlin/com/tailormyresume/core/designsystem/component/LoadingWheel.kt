package com.tailormyresume.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrLoadingWheel(
    contentDesc: String,
    modifier: Modifier = Modifier,
) {
    TmrActiveArc(
        track = TmrTheme.colors.outlineVariant,
        arc = TmrTheme.colors.primary,
        size = 40.dp,
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .testTag("loadingWheel"),
    )
}

@Preview(showBackground = true)
@Composable
private fun TmrLoadingWheelPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrLoadingWheel(contentDesc = "Loading") }
}

@Preview(showBackground = true)
@Composable
private fun TmrLoadingWheelDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrLoadingWheel(contentDesc = "Loading") }
}
