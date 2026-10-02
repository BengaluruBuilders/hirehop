package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            if (message != null) {
                Text(text = message, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
            }
            content()
            HhConfirmActions(confirmLabel, cancelLabel, onConfirm, onCancel, destructive)
        }
    }
}
