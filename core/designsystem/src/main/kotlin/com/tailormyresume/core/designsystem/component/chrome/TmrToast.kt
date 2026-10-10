package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class TmrToastAction(val label: String, val onClick: () -> Unit)

class TmrToast(val id: Long, val message: String, val action: TmrToastAction?)

class TmrToastState {
    val current: TmrToast? = null

    fun show(message: String, action: TmrToastAction? = null) = Unit

    fun dismiss() = Unit
}

@Composable
fun TmrToastHost(state: TmrToastState, modifier: Modifier = Modifier) = Unit
