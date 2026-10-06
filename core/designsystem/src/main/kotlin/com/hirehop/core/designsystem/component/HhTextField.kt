package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: (@Composable () -> Unit)? = null,
    errorText: String? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = HhTheme.colors
    var focused by remember { mutableStateOf(false) }
    val isError = errorText != null
    val shape = if (singleLine) HhTheme.shapes.field else HhTheme.shapes.card
    val fill = if (focused || isError) colors.background else colors.card
    val borderColor = when {
        isError -> colors.error
        focused -> colors.primary
        else -> colors.outlineSoft
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            Text(
                text = label,
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = HhHeightField)
                .onFocusChanged { focused = it.isFocused }
                .background(fill, shape)
                .border(if (focused || isError) 2.dp else HhWidthStroke, borderColor, shape),
            enabled = enabled,
            textStyle = HhTheme.typography.bodyL.copy(
                color = if (enabled) colors.onSurface else colors.onSurfaceVariant,
            ),
            cursorBrush = SolidColor(colors.primary),
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = remember { MutableInteractionSource() },
            decorationBox = { inner ->
                HhTextFieldDecoration(value, placeholder, trailingSlot, inner)
            },
        )
        HhTextFieldFooter(errorText = errorText, supportingText = supportingText)
    }
}

@Composable
private fun HhTextFieldDecoration(
    value: String,
    placeholder: String?,
    trailingSlot: (@Composable () -> Unit)?,
    inner: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = HhTheme.spacing.lg, vertical = HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty() && placeholder != null) {
                Text(text = placeholder, style = HhTheme.typography.bodyL, color = HhTheme.colors.onSurfaceVariant)
            }
            inner()
        }
        if (trailingSlot != null) {
            trailingSlot()
        }
    }
}

@Composable
private fun HhTextFieldFooter(errorText: String?, supportingText: (@Composable () -> Unit)?) {
    val colors = HhTheme.colors
    if (errorText != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(HhIcons.Error, contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
            Text(
                text = errorText,
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = colors.error,
            )
        }
    } else if (supportingText != null) {
        androidx.compose.material3.ProvideTextStyle(HhTheme.typography.bodyS) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides colors.onSurfaceVariant,
            ) { supportingText() }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HhTextFieldPreview() {
    HhPreviewTheme(darkTheme = false) { HhTextFieldPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhTextFieldDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhTextFieldPreviewColumn() }
}

@Composable
private fun HhTextFieldPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhTextField(value = "Android Developer", onValueChange = {}, label = "Job title")
        HhTextField(value = "", onValueChange = {}, label = "Company", placeholder = "Kestrel Labs")
        HhTextField(
            value = "",
            onValueChange = {},
            label = "Job description",
            errorText = "Paste at least 50 words so we can read the requirements.",
        )
    }
}
