package com.tailormyresume.core.designsystem.component.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TmrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    amber: Boolean = false,
) = Unit
