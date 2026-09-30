package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    HhCardSurface(
        modifier = modifier,
        shape = RoundedCornerShape(HhTheme.shapes.md),
        contentPadding = contentPadding,
        trailingAction = trailingAction,
        content = content,
    )
}

@Composable
internal fun HhCardSurface(
    modifier: Modifier,
    shape: Shape,
    contentPadding: PaddingValues?,
    trailingAction: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val padding = contentPadding ?: PaddingValues(HhTheme.spacing.lg)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = colors.surface,
        border = BorderStroke(width = HhWidthHairline, color = colors.hairline),
        shadowElevation = HhTheme.elevation.level1.elevation,
    ) {
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (trailingAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    trailingAction()
                }
            }
            content()
        }
    }
}

private const val HH_CARD_SAMPLE_TITLE = "Summer intern, Pune logistics startup"
private const val HH_CARD_SAMPLE_BODY = "Built weekly SQL reports in PostgreSQL."

@Preview(showBackground = true)
@Composable
private fun HhCardPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhCardPreviewColumn()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhCardDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhCardPreviewColumn()
    }
}

@Composable
private fun HhCardPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhCard {
            Text(
                text = HH_CARD_SAMPLE_TITLE,
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = HH_CARD_SAMPLE_BODY,
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}
