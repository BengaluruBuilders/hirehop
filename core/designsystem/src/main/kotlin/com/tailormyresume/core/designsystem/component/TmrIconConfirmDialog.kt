package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.designsystem.theme.tmrShadow

@Composable
fun TmrIconConfirmDialog(
    icon: ImageVector,
    title: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TmrIconConfirmPanel(icon, title, message, confirmLabel, cancelLabel, onConfirm, onCancel, modifier)
    }
}

@Composable
private fun TmrIconConfirmPanel(
    icon: ImageVector,
    title: String,
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
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
            Box(
                modifier = Modifier
                    .size(ICON_TILE)
                    .background(colors.errorContainer, TmrTheme.shapes.monogram),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(TmrTheme.spacing.xl),
                )
            }
            Text(
                text = title,
                style = TmrTheme.typography.titleL,
                color = colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            if (message != null) {
                Text(text = message, style = TmrTheme.typography.bodyM, color = colors.body)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TmrTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                verticalAlignment = Alignment.Bottom,
            ) {
                TmrOutlineButton(
                    label = cancelLabel,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    size = TmrButtonSize.Compact,
                )
                TmrDestructiveButton(
                    label = confirmLabel,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    size = TmrButtonSize.Compact,
                    filled = false,
                )
            }
        }
    }
}

private val ICON_TILE = 44.dp

@Preview(showBackground = true)
@Composable
private fun TmrIconConfirmDialogPreview() {
    TmrPreviewTheme(darkTheme = true) {
        TmrIconConfirmPanel(
            icon = TmrIcons.Delete,
            title = "Delete this application?",
            message = "Its resume, letter and notes are deleted. Your profile facts stay.",
            confirmLabel = "Delete",
            cancelLabel = "Cancel",
            onConfirm = {},
            onCancel = {},
        )
    }
}
