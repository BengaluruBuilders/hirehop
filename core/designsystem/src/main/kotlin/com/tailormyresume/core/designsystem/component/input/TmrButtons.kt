package com.tailormyresume.core.designsystem.component.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TmrPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDisabledClick: () -> Unit = {},
) = Unit
