package com.hirehop.feature.tailor.impl

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.TailoredBullet
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
    fun domainRestoredVerb_showsTheKeptVerbCardWhenProposedEqualsOriginal() {
        val restored = TailoredBullet(
            id = "t-restored",
            entryId = "exp-saffron",
            originalText = "Made sales reports every week.",
            proposedText = "Made sales reports every week.",
            sourceIds = listOf("exp-saffron-b1"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            decision = BulletDecision.PENDING,
            violations = listOf(GuardrailViolation.VerbEscalation("made", "built")),
        )
        val state = ReviewFixtures.state(extra = listOf(restored))
        val item = ReviewFixtures.change(state, "t-restored")

        composeRule.setContent {
            HhTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 1,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        assertThat(item.bullet.proposedText).isEqualTo(item.bullet.originalText)
        composeRule.onNode(hasText("We kept your verb")).assertExists()
    }

    @Test
    fun acceptedChange_saysTheLineIsInTheResume() {
        val state = ReviewFixtures.partlyReviewed()
        val item = ReviewFixtures.change(state, ReviewFixtures.ORDERS)

        composeRule.setContent {
            HhTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 2,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        assertThat(item.state).isEqualTo(BulletReviewState.ACCEPTED)
        composeRule.onNode(hasText("Accepted. This line is now in your resume.")).assertExists()
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
