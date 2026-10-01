package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HhConfirmSheet(
    title: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    HhBottomSheet(onDismissRequest = onCancel, modifier = modifier, title = title) {
        HhConfirmSheetBody(
            message = message,
            confirmLabel = confirmLabel,
            cancelLabel = cancelLabel,
            onConfirm = onConfirm,
            onCancel = onCancel,
            confirmColor = if (destructive) HhTheme.colors.error else HhTheme.colors.primary,
            content = content,
        )
    }
}

@Composable
private fun HhConfirmSheetBody(
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        if (message != null) {
            Text(
                text = message,
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        content()
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlinedButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = confirmLabel,
                    style = HhTheme.typography.labelLarge,
                    color = confirmColor,
                )
            }
            HhOutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = cancelLabel,
                    style = HhTheme.typography.labelLarge,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

private const val HH_CONFIRM_SHEET_SAMPLE_MESSAGE = "The tailored resume goes with it."
private const val HH_CONFIRM_SHEET_SAMPLE_CANCEL = "Keep it"
private const val HH_CONFIRM_SHEET_SAMPLE_CONFIRM = "Delete"

@Preview(showBackground = true)
@Composable
private fun HhConfirmSheetPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhConfirmSheetBody(
            message = HH_CONFIRM_SHEET_SAMPLE_MESSAGE,
            confirmLabel = HH_CONFIRM_SHEET_SAMPLE_CONFIRM,
            cancelLabel = HH_CONFIRM_SHEET_SAMPLE_CANCEL,
            onConfirm = {},
            onCancel = {},
            confirmColor = HhTheme.colors.error,
            content = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhConfirmSheetDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhConfirmSheetBody(
            message = HH_CONFIRM_SHEET_SAMPLE_MESSAGE,
            confirmLabel = HH_CONFIRM_SHEET_SAMPLE_CONFIRM,
            cancelLabel = HH_CONFIRM_SHEET_SAMPLE_CANCEL,
            onConfirm = {},
            onCancel = {},
            confirmColor = HhTheme.colors.error,
            content = {},
        )
    }
}
