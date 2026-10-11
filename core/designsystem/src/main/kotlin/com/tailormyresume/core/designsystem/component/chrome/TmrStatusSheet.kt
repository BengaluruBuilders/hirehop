package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.designsystem.component.content.TmrApplicationStatus

@Composable
fun TmrStatusSheet(
    selected: TmrApplicationStatus,
    onSelect: (TmrApplicationStatus) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
}
