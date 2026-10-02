package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    HhCardSurface(
        modifier = modifier,
        shape = HhTheme.shapes.card,
        contentPadding = contentPadding ?: PaddingValues(HhTheme.spacing.d16 + HhTheme.spacing.xxs),
        trailingAction = trailingAction,
        fill = HhTheme.colors.document,
        content = content,
    )
}

private const val HH_SECTION_CARD_SAMPLE_TITLE = "What this job asks for"
private const val HH_SECTION_CARD_SAMPLE_BODY = "You meet 9 of 14 key terms today."

@Preview(showBackground = true)
@Composable
private fun HhSectionCardPreview() {
    HhPreviewTheme(darkTheme = false) { HhSectionCardPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhSectionCardDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhSectionCardPreviewColumn() }
}

@Composable
private fun HhSectionCardPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSectionCard {
            Text(HH_SECTION_CARD_SAMPLE_TITLE, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
            Text(HH_SECTION_CARD_SAMPLE_BODY, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
        }
    }
}
