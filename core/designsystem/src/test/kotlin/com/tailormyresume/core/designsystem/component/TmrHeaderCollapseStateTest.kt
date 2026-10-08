package com.tailormyresume.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Test
import kotlin.test.assertEquals

class TmrHeaderCollapseStateTest {
    private val state = TmrHeaderCollapseState().apply { range = 200f }

    @Test
    fun scrollUpCollapsesTheHeaderBeforeTheListMoves() {
        val consumed = state.connection.onPreScroll(Offset(0f, -50f), NestedScrollSource.UserInput)

        assertEquals(-50f, consumed.y)
        assertEquals(0.25f, state.fraction)
        assertEquals(-50f, state.offset)
    }

    @Test
    fun scrollUpPastTheRangeLeavesTheRestForTheList() {
        val consumed = state.connection.onPreScroll(Offset(0f, -260f), NestedScrollSource.UserInput)

        assertEquals(-200f, consumed.y)
        assertEquals(1f, state.fraction)
    }

    @Test
    fun scrollDownLeavesTheHeaderCollapsedUntilTheListIsAtTheTop() {
        state.consume(-200f)

        val before = state.connection.onPreScroll(Offset(0f, 100f), NestedScrollSource.UserInput)
        val after = state.connection.onPostScroll(Offset.Zero, Offset(0f, 100f), NestedScrollSource.UserInput)

        assertEquals(Offset.Zero, before)
        assertEquals(100f, after.y)
        assertEquals(0.5f, state.fraction)
    }

    @Test
    fun anUnmeasuredHeaderConsumesNothing() {
        val unmeasured = TmrHeaderCollapseState()

        assertEquals(0f, unmeasured.consume(-40f))
        assertEquals(0f, unmeasured.fraction)
    }
}
