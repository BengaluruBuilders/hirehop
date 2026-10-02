package com.hirehop.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = HhTheme.colors
    val spec = HhTheme.motion.proofSpecs.color
    val fill by animateColorAsState(if (checked) colors.primary else colors.surface, spec, label = "hhCheckboxFill")
    val stroke by animateColorAsState(if (checked) colors.primary else colors.outline, spec, label = "hhCheckboxStroke")
    val box = Modifier
        .size(HhSizeCheckbox)
        .background(fill, HhTheme.shapes.tag)
        .border(2.dp, stroke, HhTheme.shapes.tag)
    Box(
        modifier = modifier
            .size(HhHeightTouch)
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
                Icon(HhIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun HhSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = HhTheme.colors
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
private fun HhCheckboxPreview() {
    HhPreviewTheme(darkTheme = false) { HhCheckboxPreviewRow() }
}

@Preview(showBackground = true)
@Composable
private fun HhCheckboxDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhCheckboxPreviewRow() }
}

@Composable
private fun HhCheckboxPreviewRow() {
    Row(modifier = Modifier.padding(HhTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
        HhCheckbox(checked = true, onCheckedChange = {})
        HhCheckbox(checked = false, onCheckedChange = {})
        HhSwitch(checked = true, onCheckedChange = {})
        HhSwitch(checked = false, onCheckedChange = {})
    }
}
