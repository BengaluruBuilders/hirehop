package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow

@Composable
fun HhCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    HhCardSurface(
        modifier = modifier,
        shape = HhTheme.shapes.card,
        contentPadding = contentPadding,
        trailingAction = trailingAction,
        onClick = onClick,
        content = content,
    )
}

@Composable
fun HhHeroCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(HhTheme.spacing.d16 + HhTheme.spacing.xxs),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HhTheme.colors
    val shape = HhTheme.shapes.heroCard
    val source = remember { MutableInteractionSource() }
    val surfaceModifier = (if (onClick == null) modifier else modifier.hhPressScale(source))
        .fillMaxWidth()
        .hhShadow(HhTheme.elevation.hero, shape)
    val border = if (HhTheme.isDark) BorderStroke(HhWidthHairline, colors.outlineVariant) else null
    if (onClick == null) {
        Surface(modifier = surfaceModifier, shape = shape, color = colors.document, border = border) {
            HhCardBody(contentPadding, null, content)
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = shape,
            color = colors.document,
            border = border,
            interactionSource = source,
        ) {
            HhCardBody(contentPadding, null, content)
        }
    }
}

@Composable
internal fun HhCardSurface(
    modifier: Modifier,
    shape: Shape,
    contentPadding: PaddingValues?,
    trailingAction: (@Composable () -> Unit)?,
    onClick: (() -> Unit)? = null,
    fill: Color = HhTheme.colors.card,
    content: @Composable ColumnScope.() -> Unit,
) {
    val border = BorderStroke(width = HhWidthHairline, color = HhTheme.colors.outlineVariant)
    val padding = contentPadding ?: PaddingValues(HhTheme.spacing.cardPadding)
    if (onClick == null) {
        Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = fill, border = border) {
            HhCardBody(padding, trailingAction, content)
        }
    } else {
        val source = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier.hhPressScale(source).fillMaxWidth(),
            shape = shape,
            color = fill,
            border = border,
            interactionSource = source,
        ) {
            HhCardBody(padding, trailingAction, content)
        }
    }
}

@Composable
private fun HhCardBody(
    padding: PaddingValues,
    trailingAction: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.padding(padding),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        if (trailingAction != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                trailingAction()
            }
        }
        content()
    }
}

private const val HH_CARD_SAMPLE_TITLE = "Android Developer"
private const val HH_CARD_SAMPLE_BODY = "Kestrel Labs · 1 to 2 years"

@Preview(showBackground = true)
@Composable
private fun HhCardPreview() {
    HhPreviewTheme(darkTheme = false) { HhCardPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhCardDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhCardPreviewColumn() }
}

@Composable
private fun HhCardPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhCard {
            Text(HH_CARD_SAMPLE_TITLE, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
            Text(HH_CARD_SAMPLE_BODY, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
        }
        HhHeroCard {
            Text(HH_CARD_SAMPLE_TITLE, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
            Text(HH_CARD_SAMPLE_BODY, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
        }
    }
}
