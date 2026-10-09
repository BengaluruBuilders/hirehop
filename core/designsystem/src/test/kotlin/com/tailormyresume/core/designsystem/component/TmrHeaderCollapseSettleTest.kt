package com.tailormyresume.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrMotionDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

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
    fun beforeHalfwayWithTheListScrolledAwayStaysWhereItIs() {
        val state = stateAt(0.3f)
        state.listAtTop = false

        settle(state)

        assertEquals(0.3f, state.fraction)
    }

    @Test
    fun reducedMotionSnapsWithoutAnimating() {
        val state = stateAt(0.75f)
        state.motion = TmrMotionDefaults.Reduced

        settle(state)

        assertEquals(1f, state.fraction)
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
