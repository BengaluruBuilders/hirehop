package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhHeroNumeral(
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    contentDescription: String? = null,
) {
    val colors = HhTheme.colors
    val semanticsModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics { this.contentDescription = contentDescription }
    }
    Column(
        modifier = semanticsModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(
            text = value,
            style = HhTheme.typography.numeralHero,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = HhTheme.typography.bodyM,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val HH_HERO_NUMERAL_SAMPLE_VALUE = "9 / 14"
private const val HH_HERO_NUMERAL_SAMPLE_CAPTION = "key terms covered"

@Preview(showBackground = true)
@Composable
private fun HhHeroNumeralPreview() {
    HhPreviewTheme(darkTheme = false) { HhHeroNumeralPreviewBody() }
}

@Preview(showBackground = true)
@Composable
private fun HhHeroNumeralDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhHeroNumeralPreviewBody() }
}

@Composable
private fun HhHeroNumeralPreviewBody() {
    HhHeroNumeral(
        value = HH_HERO_NUMERAL_SAMPLE_VALUE,
        modifier = Modifier.padding(HhTheme.spacing.lg),
        caption = HH_HERO_NUMERAL_SAMPLE_CAPTION,
    )
}
