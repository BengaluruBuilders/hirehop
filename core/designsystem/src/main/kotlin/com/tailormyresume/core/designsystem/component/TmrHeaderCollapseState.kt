package com.tailormyresume.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

@Stable
class TmrHeaderCollapseState(initialFraction: Float = 0f) {
    var fraction by mutableFloatStateOf(initialFraction)
        private set

    internal var range = 0f

    internal val offset: Float get() = -fraction * range

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (available.y < 0f) Offset(0f, consume(available.y)) else Offset.Zero

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
            if (available.y > 0f) Offset(0f, consume(available.y)) else Offset.Zero
    }

    internal fun consume(delta: Float): Float {
        if (range <= 0f) return 0f
        val next = (fraction - delta / range).coerceIn(0f, 1f)
        val consumed = (fraction - next) * range
        fraction = next
        return consumed
    }
}

@Composable
fun rememberTmrHeaderCollapseState(): TmrHeaderCollapseState =
    rememberSaveable(saver = TmrHeaderCollapseStateSaver) { TmrHeaderCollapseState() }

private val TmrHeaderCollapseStateSaver = Saver<TmrHeaderCollapseState, Float>(
    save = { it.fraction },
    restore = { TmrHeaderCollapseState(it) },
)
