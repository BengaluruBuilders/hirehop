package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class TmrTopBarLeading { Back, Close, None }

@Composable
fun TmrTopBar(
    leading: TmrTopBarLeading,
    onLeading: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
) = Unit
