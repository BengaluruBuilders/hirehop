package com.tailormyresume.feature.tailor.impl

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.EditType
import com.tailormyresume.core.model.TailoredBullet
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class BulletReviewAddedWordsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun educationChange_legendShowsDbmsNotPlaceholder() {
        val state = ReviewFixtures.state()
        val item = ReviewFixtures.change(state, ReviewFixtures.EDUCATION)

        composeRule.setContent {
            TmrTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 4,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        composeRule.onNode(hasText("Added words")).assertExists()
        composeRule.onNode(hasText("DBMS")).assertExists()
        assertThat(composeRule.onAllNodes(hasText("abc")).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun severalAddedWords_listInOrderWithoutDuplicates() {
        val bullet = TailoredBullet(
            id = "t-added-words",
            entryId = "exp-saffron",
            originalText = "Made sales reports every week.",
            proposedText = "Built weekly sales reports and weekly dashboards every week.",
            sourceIds = listOf("exp-saffron-b1"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            decision = BulletDecision.PENDING,
            violations = emptyList(),
        )
        val state = ReviewFixtures.state(extra = listOf(bullet))
        val item = ReviewFixtures.change(state, "t-added-words")

        composeRule.setContent {
            TmrTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 6,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        val entries = listOf("Built", "weekly", "and", "dashboards").map { word ->
            word to composeRule.onAllNodes(hasText(word)).fetchSemanticsNodes().single().boundsInRoot
        }
        val sorted = entries.sortedWith(compareBy({ it.second.top }, { it.second.left })).map { it.first }

        assertThat(sorted).containsExactly("Built", "weekly", "and", "dashboards").inOrder()
        assertThat(composeRule.onAllNodes(hasText("abc")).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun removalOnlyChange_hidesTheLegend() {
        val bullet = TailoredBullet(
            id = "t-removal-only",
            entryId = "exp-saffron",
            originalText = "Made sales reports every week.",
            proposedText = "Made reports every week.",
            sourceIds = listOf("exp-saffron-b1"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            decision = BulletDecision.PENDING,
            violations = emptyList(),
        )
        val state = ReviewFixtures.state(extra = listOf(bullet))
        val item = ReviewFixtures.change(state, "t-removal-only")

        composeRule.setContent {
            TmrTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 6,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        composeRule.onAllNodes(hasText("Added words")).assertCountEquals(0)
        composeRule.onNode(hasText("New line", ignoreCase = true)).assertExists()
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
