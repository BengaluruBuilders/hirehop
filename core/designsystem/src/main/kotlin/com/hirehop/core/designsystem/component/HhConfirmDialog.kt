package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhConfirmDialog(
    title: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    destructive: Boolean = false,
) {
    val colors = HhTheme.colors
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(HhTheme.spacing.d24),
            shape = RoundedCornerShape(HhTheme.shapes.lg),
            color = colors.surface,
            border = BorderStroke(width = HhWidthHairline, color = colors.hairline),
            shadowElevation = HhTheme.elevation.level3.elevation,
        ) {
            Column(
                modifier = Modifier.padding(HhTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                Text(
                    text = title,
                    style = HhTheme.typography.titleLarge,
                    color = colors.onSurface,
                )
                if (message != null) {
                    Text(
                        text = message,
                        style = HhTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
                HhConfirmActions(
                    confirmLabel = confirmLabel,
                    cancelLabel = cancelLabel,
                    onConfirm = onConfirm,
                    onCancel = onCancel,
                    confirmColor = if (destructive) colors.error else colors.primary,
                )
            }
        }
    }
}

@Composable
internal fun HhConfirmActions(
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = HhTheme.spacing.sm,
            alignment = Alignment.End,
        ),
    ) {
        HhOutlinedButton(onClick = onCancel) {
            Text(
                text = cancelLabel,
                style = HhTheme.typography.labelLarge,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        HhOutlinedButton(onClick = onConfirm) {
            Text(
                text = confirmLabel,
                style = HhTheme.typography.labelLarge,
                color = confirmColor,
            )
        }
    }
}

private const val HH_CONFIRM_DIALOG_SAMPLE_TITLE = "Delete this application?"
private const val HH_CONFIRM_DIALOG_SAMPLE_MESSAGE = "The tailored resume goes with it."
private const val HH_CONFIRM_DIALOG_SAMPLE_CANCEL = "Keep it"
private const val HH_CONFIRM_DIALOG_SAMPLE_CONFIRM = "Delete"

@Preview(showBackground = true)
@Composable
private fun HhConfirmDialogPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhConfirmDialog(
            title = HH_CONFIRM_DIALOG_SAMPLE_TITLE,
            message = HH_CONFIRM_DIALOG_SAMPLE_MESSAGE,
            confirmLabel = HH_CONFIRM_DIALOG_SAMPLE_CONFIRM,
            cancelLabel = HH_CONFIRM_DIALOG_SAMPLE_CANCEL,
            onConfirm = {},
            onCancel = {},
            destructive = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhConfirmDialogDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhConfirmDialog(
            title = HH_CONFIRM_DIALOG_SAMPLE_TITLE,
            message = HH_CONFIRM_DIALOG_SAMPLE_MESSAGE,
            confirmLabel = HH_CONFIRM_DIALOG_SAMPLE_CONFIRM,
            cancelLabel = HH_CONFIRM_DIALOG_SAMPLE_CANCEL,
            onConfirm = {},
            onCancel = {},
            destructive = true,
        )
    }
}
