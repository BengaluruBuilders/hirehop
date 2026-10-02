package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSpecialButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.hhPressScale(source).fillMaxWidth(),
        shape = HhTheme.shapes.pill,
        color = HhTheme.colors.special,
        contentColor = HhTheme.colors.onSpecial,
        interactionSource = source,
    ) {
        HhLargeButtonBody(label = label, caption = caption, color = HhTheme.colors.onSpecial)
    }
}

@Composable
fun HhSpecialDeclineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.hhPressScale(source).fillMaxWidth(),
        shape = HhTheme.shapes.pill,
        color = Color.Transparent,
        contentColor = HhTheme.colors.onSurface,
        border = BorderStroke(HhWidthStroke, HhTheme.colors.outline),
        interactionSource = source,
    ) {
        HhLargeButtonBody(label = label, caption = null, color = HhTheme.colors.onSurface)
    }
}

@Composable
fun HhSpecialOffer(
    label: String,
    declineLabel: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhSpecialButton(label = label, onClick = onAccept, caption = caption)
        HhSpecialDeclineButton(label = declineLabel, onClick = onDecline)
    }
}

@Composable
private fun HhLargeButtonBody(label: String, caption: String?, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.sm)
            .heightInLarge(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = label,
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.ExtraBold),
            color = color,
            textAlign = TextAlign.Center,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = HhTheme.typography.labelM,
                color = color,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Modifier.heightInLarge(): Modifier =
    heightIn(min = HhHeightButtonLarge - 16.dp)

@Preview(showBackground = true)
@Composable
private fun HhSpecialOfferPreview() {
    HhPreviewTheme(darkTheme = false) { HhSpecialOfferSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhSpecialOfferDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhSpecialOfferSample() }
}

@Composable
private fun HhSpecialOfferSample() {
    HhSpecialOffer(
        label = "5 applications · ₹149",
        declineLabel = "Not now",
        onAccept = {},
        onDecline = {},
        modifier = Modifier.padding(HhTheme.spacing.gutter),
        caption = "Credits never expire",
    )
}
