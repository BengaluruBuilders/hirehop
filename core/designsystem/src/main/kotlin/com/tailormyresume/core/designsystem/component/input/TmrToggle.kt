package com.tailormyresume.core.designsystem.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TmrTheme.colors
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .background(if (checked) colors.lime else colors.lineStrong, TmrTheme.shapes.pill)
            .padding(4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (checked) colors.ink else colors.textMuted, TmrTheme.shapes.pill),
        )
    }
}
