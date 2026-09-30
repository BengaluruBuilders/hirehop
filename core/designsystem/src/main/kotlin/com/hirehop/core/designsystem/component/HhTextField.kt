package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
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
    val shape = RoundedCornerShape(HhTheme.shapes.md)
    val isError = errorText != null
    var focused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = when {
        isError -> colors.error
        focused -> colors.primary
        else -> colors.hairlineStrong
    }
    val borderWidth = if (focused) HhWidthStroke else HhWidthHairline
    val textStyle = HhTheme.typography.bodyLarge.copy(
        color = if (enabled) colors.onSurface else colors.onSurfaceVariant,
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = HhTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .border(width = borderWidth, color = borderColor, shape = shape)
                .background(color = colors.surface, shape = shape)
                .padding(
                    horizontal = HhTheme.spacing.md,
                    vertical = HhTheme.spacing.sm,
                ),
            enabled = enabled,
            textStyle = textStyle,
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { innerTextField ->
                HhTextFieldDecoration(
                    value = value,
                    placeholder = placeholder,
                    textStyle = textStyle,
                    trailingSlot = trailingSlot,
                    innerTextField = innerTextField,
                )
            },
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = HhTheme.typography.bodySmall,
                color = colors.error,
            )
        } else if (supportingText != null) {
            supportingText()
        }
    }
}

@Composable
private fun HhTextFieldDecoration(
    value: String,
    placeholder: String?,
    textStyle: TextStyle,
    trailingSlot: (@Composable () -> Unit)?,
    innerTextField: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = textStyle,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            innerTextField()
        }
        if (trailingSlot != null) {
            trailingSlot()
        }
    }
}

private const val HH_TEXT_FIELD_SAMPLE_LABEL = "Job description"
private const val HH_TEXT_FIELD_SAMPLE_PLACEHOLDER = "Paste the job description here"
private const val HH_TEXT_FIELD_SAMPLE_SUPPORTING = "Stored on this phone. Never sent."

@Preview(showBackground = true)
@Composable
private fun HhTextFieldPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhTextField(
            value = "",
            onValueChange = {},
            label = HH_TEXT_FIELD_SAMPLE_LABEL,
            placeholder = HH_TEXT_FIELD_SAMPLE_PLACEHOLDER,
            supportingText = {
                Text(
                    text = HH_TEXT_FIELD_SAMPLE_SUPPORTING,
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            },
            modifier = Modifier.padding(HhTheme.spacing.lg),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhTextFieldDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhTextField(
            value = "",
            onValueChange = {},
            label = HH_TEXT_FIELD_SAMPLE_LABEL,
            placeholder = HH_TEXT_FIELD_SAMPLE_PLACEHOLDER,
            supportingText = {
                Text(
                    text = HH_TEXT_FIELD_SAMPLE_SUPPORTING,
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            },
            modifier = Modifier.padding(HhTheme.spacing.lg),
        )
    }
}
