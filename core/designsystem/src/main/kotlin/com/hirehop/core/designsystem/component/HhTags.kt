package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhFactId(
    id: String,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    Text(
        text = id,
        modifier = modifier
            .heightIn(min = HhHeightTagSmall)
            .background(colors.primaryContainer, HhTheme.shapes.tag)
            .padding(horizontal = 10.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        style = HhTheme.typography.factId,
        color = colors.primary,
    )
}

@Preview(showBackground = true)
@Composable
private fun HhTagsPreview() {
    HhPreviewTheme(darkTheme = false) { HhTagsPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhTagsDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhTagsPreviewColumn() }
}

@Composable
private fun HhTagsPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhFactId(id = "F-031")
        }
    }
}
