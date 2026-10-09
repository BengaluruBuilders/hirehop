package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.component.TmrHeaderCollapseState
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w427dp-h952dp-normal-long-notround-any-480dpi-keyshidden-nonav")
class ApplicationsShortListHeaderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val collapse = TmrHeaderCollapseState()

    @Test
    fun aSlowShortSwipeSettlesTheHeaderAtAnEnd() {
        show()

        swipeUp(distance = 100f, durationMillis = 300)

        assertThat(collapse.fraction).isAnyOf(0f, 1f)
    }

    @Test
    fun aSlowerLongerSwipeSettlesTheHeaderAtAnEnd() {
        show()

        swipeUp(distance = 190f, durationMillis = 900)

        assertThat(collapse.fraction).isAnyOf(0f, 1f)
    }

    @Test
    fun whenCollapsedTheListHeadingSitsUnderTheCompactHeader() {
        show()

        swipeUp(distance = 250f, durationMillis = 100)

        assertThat(collapse.fraction).isEqualTo(1f)
        val heading = composeRule.onNodeWithText("Your applications").getUnclippedBoundsInRoot()
        val pasteJob = composeRule.onNodeWithText("Paste a job").getUnclippedBoundsInRoot()
        assertThat(heading.top - pasteJob.bottom).isLessThan(48.dp)
    }

    private fun swipeUp(distance: Float, durationMillis: Long) {
        composeRule.onRoot().performTouchInput {
            swipeUp(startY = SWIPE_START_Y, endY = SWIPE_START_Y - distance, durationMillis = durationMillis)
        }
        composeRule.waitForIdle()
    }

    private fun show() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                Box(Modifier.height(SCROLLABLE_BY_A_LITTLE_DP.dp)) {
                    ApplicationsScreen(
                        uiState = previewListState(),
                        onAction = {},
                        collapse = collapse,
                        now = PREVIEW_INSTANT,
                    )
                }
            }
        }
    }

    private companion object {
        const val SWIPE_START_Y = 1500f
        const val SCROLLABLE_BY_A_LITTLE_DP = 800
    }
}
