package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSheet(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = HhTheme.spacing.gutter,
        top = HhTheme.spacing.d24,
        end = HhTheme.spacing.gutter,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HhTheme.shapes.sheet,
        color = HhTheme.colors.surface,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun HhSheetPreview() {
    HhPreviewTheme(darkTheme = false) { HhSheetSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhSheetDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhSheetSample() }
}

@Composable
private fun HhSheetSample() {
    HhSheet {
        Text(text = "Your applications", style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
    }
}
