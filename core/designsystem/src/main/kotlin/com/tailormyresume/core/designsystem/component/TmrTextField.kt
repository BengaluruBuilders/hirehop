package com.tailormyresume.core.designsystem.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

private val TmrTextAreaShape = RoundedCornerShape(22.dp)

@Composable
fun TmrTextField(
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
    val colors = TmrTheme.colors
    var focused by remember { mutableStateOf(false) }
    val isError = errorText != null
    val shape = if (singleLine) TmrTheme.shapes.field else TmrTextAreaShape
    val borderColor = when {
        isError -> colors.error
        focused -> colors.primary
        else -> colors.boundary
    }
    val borderWidth = if (isError || focused) TmrWidthStrokeFocus else TmrWidthHairline
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = TmrHeightField)
                .onFocusChanged { focused = it.isFocused }
                .semantics { if (errorText != null) error(errorText) }
                .background(colors.card, shape)
                .border(borderWidth, borderColor, shape),
            enabled = enabled,
            textStyle = TmrTheme.typography.bodyL.copy(
                fontWeight = FontWeight.Bold,
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
                TmrTextFieldDecoration(value, label, placeholder, trailingSlot, inner)
            },
        )
        TmrTextFieldFooter(errorText = errorText, supportingText = supportingText)
    }
}

@Composable
private fun TmrTextFieldDecoration(
    value: String,
    label: String?,
    placeholder: String?,
    trailingSlot: (@Composable () -> Unit)?,
    inner: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = TmrTheme.spacing.lg, vertical = TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (label != null) {
                Text(text = label, style = TmrTheme.typography.labelM, color = TmrTheme.colors.onSurfaceVariant)
            }
            Box {
                if (value.isEmpty() && placeholder != null) {
                    Text(text = placeholder, style = TmrTheme.typography.bodyL, color = TmrTheme.colors.onSurfaceVariant)
                }
                inner()
            }
        }
        if (trailingSlot != null) {
            trailingSlot()
        }
    }
}

@Composable
private fun TmrTextFieldFooter(errorText: String?, supportingText: (@Composable () -> Unit)?) {
    val colors = TmrTheme.colors
    if (errorText != null) {
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TmrIcons.Error, contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
            Text(
                text = errorText,
                style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = colors.error,
            )
        }
    } else if (supportingText != null) {
        androidx.compose.material3.ProvideTextStyle(TmrTheme.typography.bodyS) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides colors.onSurfaceVariant,
            ) { supportingText() }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TmrTextFieldPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrTextFieldPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun TmrTextFieldDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrTextFieldPreviewColumn() }
}

@Composable
private fun TmrTextFieldPreviewColumn() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrTextField(value = "Android Developer", onValueChange = {}, label = "Job title")
        TmrTextField(value = "", onValueChange = {}, label = "Company", placeholder = "Kestrel Labs")
        TmrTextField(
            value = "",
            onValueChange = {},
            label = "Job description",
            errorText = "Paste at least 50 words so we can read the requirements.",
        )
    }
}
