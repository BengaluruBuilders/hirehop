package com.tailormyresume.core.designsystem.component.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TmrListRow(
    label: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    tag: TmrTag? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    showDivider: Boolean = true,
) {
}
