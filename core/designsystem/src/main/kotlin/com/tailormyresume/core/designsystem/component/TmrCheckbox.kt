package com.tailormyresume.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

private val TmrCheckboxShape = RoundedCornerShape(7.dp)

@Composable
fun TmrCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TmrTheme.colors
    val spec = TmrTheme.motion.proofSpecs.color
    val fill by animateColorAsState(if (checked) colors.brand else Color.Transparent, spec, label = "tmrCheckboxFill")
    val stroke by animateColorAsState(if (checked) colors.brand else colors.onSurfaceVariant, spec, label = "tmrCheckboxStroke")
    val box = Modifier
        .size(TmrSizeCheckbox)
        .background(fill, TmrCheckboxShape)
        .border(2.dp, stroke, TmrCheckboxShape)
    Box(
        modifier = modifier
            .size(TmrHeightTouch)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = box, contentAlignment = Alignment.Center) {
            if (checked) {
                Icon(TmrIcons.Check, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun TmrSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TmrTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            uncheckedThumbColor = colors.outline,
            uncheckedTrackColor = colors.ground,
            uncheckedBorderColor = colors.outline,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun TmrCheckboxPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrCheckboxPreviewRow() }
}

@Preview(showBackground = true)
@Composable
private fun TmrCheckboxDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrCheckboxPreviewRow() }
}

@Composable
private fun TmrCheckboxPreviewRow() {
    Row(modifier = Modifier.padding(TmrTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
        TmrCheckbox(checked = true, onCheckedChange = {})
        TmrCheckbox(checked = false, onCheckedChange = {})
        TmrSwitch(checked = true, onCheckedChange = {})
        TmrSwitch(checked = false, onCheckedChange = {})
    }
}
