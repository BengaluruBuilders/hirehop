package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.designsystem.theme.tmrShadow

@Composable
fun TmrConfirmDialog(
    title: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TmrConfirmPanel(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            cancelLabel = cancelLabel,
            onConfirm = onConfirm,
            onCancel = onCancel,
            destructive = destructive,
            modifier = modifier,
            icon = icon,
        )
    }
}

@Composable
internal fun TmrConfirmPanel(
    title: String,
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    destructive: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = TmrTheme.colors
    val shape = RoundedCornerShape(TmrRadiusSheet)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(TmrTheme.spacing.d24)
            .tmrShadow(TmrTheme.elevation.modal, shape)
            .semantics { paneTitle = title },
        shape = shape,
        color = colors.surface,
        border = if (TmrTheme.isDark) BorderStroke(TmrWidthHairline, colors.outlineSoft) else null,
    ) {
        Column(
            modifier = Modifier.padding(TmrTheme.spacing.d24),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.errorContainer)
                        .testTag("confirmIconTile"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            TmrFitText(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = TmrTheme.typography.titleL,
                color = colors.onSurface,
            )
            if (message != null) {
                TmrFitText(text = message, style = TmrTheme.typography.bodyM, color = colors.body)
            }
            TmrConfirmActions(confirmLabel, cancelLabel, onConfirm, onCancel, destructive)
        }
    }
}

@Composable
internal fun TmrConfirmActions(
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    destructive: Boolean,
) {
    Column(
        modifier = Modifier.padding(top = TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrOutlineButton(label = cancelLabel, onClick = onCancel, modifier = Modifier.fillMaxWidth())
        if (destructive) {
            TmrDestructiveButton(label = confirmLabel, onClick = onConfirm, modifier = Modifier.fillMaxWidth())
        } else {
            TmrPrimaryButton(label = confirmLabel, onClick = onConfirm, modifier = Modifier.fillMaxWidth())
        }
    }
}

private const val TMR_CONFIRM_DIALOG_SAMPLE_TITLE = "Delete your account?"
private const val TMR_CONFIRM_DIALOG_SAMPLE_MESSAGE = "This removes your 23 facts, 4 resumes and history. You cannot undo it."
private const val TMR_CONFIRM_DIALOG_SAMPLE_CANCEL = "Keep my account"
private const val TMR_CONFIRM_DIALOG_SAMPLE_CONFIRM = "Delete account"

@Preview(showBackground = true)
@Composable
private fun TmrConfirmDialogPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrConfirmDialogPanelSample() }
}

@Preview(showBackground = true)
@Composable
private fun TmrConfirmDialogDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrConfirmDialogPanelSample() }
}

@Composable
private fun TmrConfirmDialogPanelSample() {
    TmrConfirmPanel(
        title = TMR_CONFIRM_DIALOG_SAMPLE_TITLE,
        message = TMR_CONFIRM_DIALOG_SAMPLE_MESSAGE,
        confirmLabel = TMR_CONFIRM_DIALOG_SAMPLE_CONFIRM,
        cancelLabel = TMR_CONFIRM_DIALOG_SAMPLE_CANCEL,
        onConfirm = {},
        onCancel = {},
        destructive = true,
    )
}
