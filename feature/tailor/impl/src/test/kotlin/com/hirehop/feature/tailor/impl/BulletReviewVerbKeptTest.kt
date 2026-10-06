package com.hirehop.feature.tailor.impl

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.screenshot.HhTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class BulletReviewVerbKeptTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun flaggedVerb_showsTheKeptVerbAndNeverTheEscalatedOne() {
        val state = ReviewFixtures.state(
            weeklyViolations = listOf(GuardrailViolation.VerbEscalation("made", "built")),
        )
        val item = ReviewFixtures.change(state, ReviewFixtures.WEEKLY)

        composeRule.setContent {
            HhTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 1,
                    total = state.totalCount,
                    openCount = state.openCount,
                    actions = emptyActions(),
                )
            }
        }

        assertThat(item.bullet.proposedText).startsWith("Made weekly")
        assertThat(
            composeRule.onAllNodes(hasText("Made weekly sales reports", substring = true)).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodes(hasText("Built weekly", substring = true)).fetchSemanticsNodes(),
        ).isEmpty()
    }

    @Test
    fun acceptedChange_showsTheRemainingCount() {
        val state = ReviewFixtures.partlyReviewed()
        val item = ReviewFixtures.change(state, ReviewFixtures.ORDERS)

        composeRule.setContent {
            HhTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 2,
                    total = state.totalCount,
                    openCount = state.openCount,
                    actions = emptyActions(),
                )
            }
        }

        assertThat(state.openCount).isGreaterThan(0)
        composeRule.onAllNodes(
            hasText("Accepted. ${state.openCount} left to review.", substring = true),
        ).fetchSemanticsNodes().let { assertThat(it).isNotEmpty() }
    }

    private fun emptyActions() = BulletSheetActions(
        onPrevious = null,
        onNext = null,
        onAccept = {},
        onKeepOriginal = {},
        onUndo = {},
        onEditByHand = {},
        onNextChange = {},
        onOpenSource = {},
        onReport = {},
    )
}
