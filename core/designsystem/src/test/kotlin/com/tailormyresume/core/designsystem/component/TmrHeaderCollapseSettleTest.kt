package com.tailormyresume.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Velocity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrMotionDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrHeaderCollapseSettleTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var scope: CoroutineScope

    @Test
    fun pastHalfwayTheFlingEndSettlesCollapsed() {
        val state = stateAt(0.75f)

        settle(state)

        assertEquals(1f, state.fraction)
    }

    @Test
    fun beforeHalfwayAtTheTopTheFlingEndSettlesExpanded() {
        val state = stateAt(0.3f)

        settle(state)

        assertEquals(0f, state.fraction)
    }

    @Test
    fun beforeHalfwayWithTheListScrolledAwaySettlesCollapsed() {
        val state = stateAt(0.3f)
        state.listAtTop = false

        settle(state)

        assertEquals(1f, state.fraction)
    }

    @Test
    fun reducedMotionSnapsWithoutAnimating() {
        val state = stateAt(0.75f)
        state.motion = TmrMotionDefaults.Reduced

        settle(state)

        assertEquals(1f, state.fraction)
    }

    @Test
    fun aScrollDuringTheSettleStopsItFromMovingTheHeader() {
        val state = stateAt(0.75f).apply { range = 200f }
        rule.mainClock.autoAdvance = false

        scope.launch { state.settle() }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        state.connection.onPreScroll(Offset(0f, 40f), NestedScrollSource.UserInput)
        val atInterrupt = state.fraction
        rule.mainClock.advanceTimeBy(1000)

        assertEquals(atInterrupt, state.fraction)
    }

    @Test
    fun theNestedScrollConnectionTracksTheListAndSettlesOnFlingEnd() {
        val state = TmrHeaderCollapseState().apply {
            range = 200f
            motion = TmrMotionDefaults.Reduced
        }
        val connection = state.connection
        val user = NestedScrollSource.UserInput

        connection.onPostScroll(Offset(0f, -10f), Offset.Zero, user)
        assertFalse(state.listAtTop)

        connection.onPreScroll(Offset(0f, -150f), user)
        runBlocking { connection.onPostFling(Velocity.Zero, Velocity.Zero) }
        assertEquals(1f, state.fraction)

        connection.onPostScroll(Offset.Zero, Offset(0f, 130f), user)
        assertTrue(state.listAtTop)
        connection.onPostScroll(Offset.Zero, Offset(0f, 10f), user)
        runBlocking { connection.onPostFling(Velocity.Zero, Velocity.Zero) }
        assertEquals(0f, state.fraction)
    }

    private fun stateAt(fraction: Float): TmrHeaderCollapseState {
        rule.setContent { scope = rememberCoroutineScope() }
        return TmrHeaderCollapseState(initialFraction = fraction)
    }

    private fun settle(state: TmrHeaderCollapseState) {
        scope.launch { state.settle() }
        rule.waitForIdle()
    }
}
