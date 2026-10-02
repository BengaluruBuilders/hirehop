package com.hirehop.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow

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
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        HhConfirmPanel(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            cancelLabel = cancelLabel,
            onConfirm = onConfirm,
            onCancel = onCancel,
            destructive = destructive,
            modifier = modifier,
        )
    }
}

@Composable
internal fun HhConfirmPanel(
    title: String,
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    destructive: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val shape = RoundedCornerShape(HhRadiusSheet)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(HhTheme.spacing.d24)
            .hhShadow(HhTheme.elevation.modal, shape),
        shape = shape,
        color = colors.surface,
        border = if (HhTheme.isDark) BorderStroke(HhWidthHairline, colors.outlineSoft) else null,
    ) {
        Column(
            modifier = Modifier.padding(HhTheme.spacing.d24),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Text(text = title, style = HhTheme.typography.titleL, color = colors.onSurface)
            if (message != null) {
                Text(text = message, style = HhTheme.typography.bodyM, color = colors.body)
            }
            HhConfirmActions(confirmLabel, cancelLabel, onConfirm, onCancel, destructive)
        }
    }
}

@Composable
internal fun HhConfirmActions(
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    destructive: Boolean,
) {
    Column(
        modifier = Modifier.padding(top = HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        if (destructive) {
            HhDestructiveButton(label = confirmLabel, onClick = onConfirm, modifier = Modifier.fillMaxWidth())
        } else {
            HhPrimaryButton(label = confirmLabel, onClick = onConfirm, modifier = Modifier.fillMaxWidth())
        }
        HhOutlineButton(label = cancelLabel, onClick = onCancel, modifier = Modifier.fillMaxWidth())
    }
}

private const val HH_CONFIRM_DIALOG_SAMPLE_TITLE = "Delete your account?"
private const val HH_CONFIRM_DIALOG_SAMPLE_MESSAGE = "This removes your 23 facts, 4 resumes and history. You cannot undo it."
private const val HH_CONFIRM_DIALOG_SAMPLE_CANCEL = "Keep my account"
private const val HH_CONFIRM_DIALOG_SAMPLE_CONFIRM = "Delete account"

@Preview(showBackground = true)
@Composable
private fun HhConfirmDialogPreview() {
    HhPreviewTheme(darkTheme = false) { HhConfirmDialogPanelSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhConfirmDialogDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhConfirmDialogPanelSample() }
}

@Composable
private fun HhConfirmDialogPanelSample() {
    HhConfirmPanel(
        title = HH_CONFIRM_DIALOG_SAMPLE_TITLE,
        message = HH_CONFIRM_DIALOG_SAMPLE_MESSAGE,
        confirmLabel = HH_CONFIRM_DIALOG_SAMPLE_CONFIRM,
        cancelLabel = HH_CONFIRM_DIALOG_SAMPLE_CANCEL,
        onConfirm = {},
        onCancel = {},
        destructive = true,
    )
}
