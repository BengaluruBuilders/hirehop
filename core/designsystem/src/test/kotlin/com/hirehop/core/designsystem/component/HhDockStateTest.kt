package com.hirehop.core.designsystem.component

import androidx.compose.runtime.mutableStateOf
import com.hirehop.core.designsystem.theme.HhMotionDefaults
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class HhDockStateTest {
    private val reach = 50f

    @Test
    fun firstMoveSnapsTheNotchAndRaisesTheBall() = runBlocking {
        val state = HhDockState()
        val span = HhDockSpan(mutableStateOf(true))

        state.moveTo(span, centerX = 120f, reach = reach, motion = HhMotionDefaults.Default)

        assertSame(span, state.current)
        assertEquals(120f, state.notchX.value)
        assertEquals(1f, state.lift.value)
    }

    @Test
    fun reducedMotionSnapsToTheNextItem() = runBlocking {
        val state = HhDockState()
        state.moveTo(HhDockSpan(mutableStateOf(false)), centerX = 120f, reach = reach, motion = HhMotionDefaults.Reduced)

        state.moveTo(HhDockSpan(mutableStateOf(true)), centerX = 300f, reach = reach, motion = HhMotionDefaults.Reduced)

        assertEquals(300f, state.notchX.value)
        assertEquals(1f, state.lift.value)
    }

    @Test
    fun onlyTheItemUnderTheNotchIsLifted() = runBlocking {
        val state = HhDockState()
        state.moveTo(HhDockSpan(mutableStateOf(true)), centerX = 120f, reach = reach, motion = HhMotionDefaults.Default)

        assertEquals(1f, state.liftAt(centerX = 120f, reach = reach))
        assertEquals(0.5f, state.liftAt(centerX = 145f, reach = reach))
        assertEquals(0f, state.liftAt(centerX = 300f, reach = reach))
        assertEquals(0f, state.liftAt(centerX = Float.NaN, reach = reach))
    }
}
