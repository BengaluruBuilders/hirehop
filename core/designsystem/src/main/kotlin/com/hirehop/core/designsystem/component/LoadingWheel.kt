package com.hirehop.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhLoadingWheel(
    contentDesc: String,
    modifier: Modifier = Modifier,
) {
    HhActiveArc(
        track = HhTheme.colors.outlineVariant,
        arc = HhTheme.colors.primary,
        size = 40.dp,
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .testTag("loadingWheel"),
    )
}

@Preview(showBackground = true)
@Composable
private fun HhLoadingWheelPreview() {
    HhPreviewTheme(darkTheme = false) { HhLoadingWheel(contentDesc = "Loading") }
}

@Preview(showBackground = true)
@Composable
private fun HhLoadingWheelDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhLoadingWheel(contentDesc = "Loading") }
}
