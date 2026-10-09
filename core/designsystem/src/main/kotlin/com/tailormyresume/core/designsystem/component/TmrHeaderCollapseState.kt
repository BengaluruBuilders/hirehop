package com.tailormyresume.core.designsystem.component

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.tailormyresume.core.designsystem.theme.TmrMotion
import com.tailormyresume.core.designsystem.theme.TmrMotionDefaults
import com.tailormyresume.core.designsystem.theme.TmrTheme
import kotlinx.coroutines.CancellationException

@Stable
class TmrHeaderCollapseState(initialFraction: Float = 0f) {
    var fraction by mutableFloatStateOf(initialFraction)
        private set

    internal var range = 0f

    internal var motion: TmrMotion = TmrMotionDefaults.Default

    internal var listAtTop = true

    private var inputCount = 0

    internal val offset: Float get() = -fraction * range

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            inputCount++
            return if (available.y < 0f) Offset(0f, consume(available.y)) else Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (consumed.y != 0f) listAtTop = false
            if (available.y <= 0f) return Offset.Zero
            listAtTop = true
            return Offset(0f, consume(available.y))
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }

    suspend fun settle() {
        val target = when {
            fraction >= SETTLE_THRESHOLD -> 1f
            listAtTop -> 0f
            else -> return
        }
        if (motion.reduced) {
            fraction = target
        } else {
            val startInput = inputCount
            animate(fraction, target, animationSpec = motion.proofSpecs.spatial) { value, _ ->
                if (inputCount != startInput) throw CancellationException()
                fraction = value
            }
        }
    }

    internal fun consume(delta: Float): Float {
        inputCount++
        if (range <= 0f) return 0f
        val next = (fraction - delta / range).coerceIn(0f, 1f)
        val consumed = (fraction - next) * range
        fraction = next
        return consumed
    }
}

@Composable
fun rememberTmrHeaderCollapseState(): TmrHeaderCollapseState {
    val state = rememberSaveable(saver = TmrHeaderCollapseStateSaver) { TmrHeaderCollapseState() }
    val motion = TmrTheme.motion
    SideEffect { state.motion = motion }
    return state
}

private const val SETTLE_THRESHOLD = 0.5f

private val TmrHeaderCollapseStateSaver = Saver<TmrHeaderCollapseState, Float>(
    save = { it.fraction },
    restore = { TmrHeaderCollapseState(it) },
)
