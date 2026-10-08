package com.tailormyresume.core.designsystem.component

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
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrFactId(
    id: String,
    modifier: Modifier = Modifier,
) {
    val colors = TmrTheme.colors
    Text(
        text = id,
        modifier = modifier
            .heightIn(min = TmrHeightTagSmall)
            .background(colors.primaryContainer, TmrTheme.shapes.tag)
            .padding(horizontal = 10.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        style = TmrTheme.typography.factId,
        color = colors.primary,
    )
}

@Preview(showBackground = true)
@Composable
private fun TmrTagsPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrTagsPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun TmrTagsDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrTagsPreviewColumn() }
}

@Composable
private fun TmrTagsPreviewColumn() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            TmrFactId(id = "F-031")
        }
    }
}
