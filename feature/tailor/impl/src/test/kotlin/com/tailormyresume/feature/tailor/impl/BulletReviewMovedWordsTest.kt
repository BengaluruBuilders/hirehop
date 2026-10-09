package com.tailormyresume.feature.tailor.impl

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.EditType
import com.tailormyresume.core.model.EvidenceBullet
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
class BulletReviewMovedWordsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reorderedLine_legendIsHidden() {
        val bullet = TailoredBullet(
            id = "t-moved-words",
            entryId = "exp-saffron",
            originalText = "Cleaned and validated daily sales data from 40 stores using Excel and SQL.",
            proposedText = "Using Excel and SQL, cleaned and validated daily sales data from 40 stores.",
            sourceIds = listOf("exp-saffron-moved"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            decision = BulletDecision.PENDING,
            violations = emptyList(),
        )
        val state = stateFor(bullet)
        val item = ReviewFixtures.change(state, "t-moved-words")

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
    }

    @Test
    fun reorderedLineWithOneNewWord_listsOnlyTheNewWord() {
        val bullet = TailoredBullet(
            id = "t-moved-words-one-new",
            entryId = "exp-saffron",
            originalText = "Cleaned and validated daily sales data from 40 stores using Excel and SQL.",
            proposedText = "Using Python and SQL, cleaned and validated daily sales data from 40 stores.",
            sourceIds = listOf("exp-saffron-moved"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            decision = BulletDecision.PENDING,
            violations = emptyList(),
        )
        val state = stateFor(bullet)
        val item = ReviewFixtures.change(state, "t-moved-words-one-new")

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

        composeRule.onNode(hasText("Added words")).assertExists()
        composeRule.onNode(hasText("Python")).assertExists()
        assertThat(composeRule.onAllNodes(hasText("Using")).fetchSemanticsNodes()).isEmpty()
        assertThat(composeRule.onAllNodes(hasText("SQL,")).fetchSemanticsNodes()).isEmpty()
        assertThat(composeRule.onAllNodes(hasText("cleaned")).fetchSemanticsNodes()).isEmpty()
        assertThat(composeRule.onAllNodes(hasText("and")).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun caseOnlyDifference_isNotListed() {
        val bullet = TailoredBullet(
            id = "t-case-only",
            entryId = "exp-saffron",
            originalText = "using Excel daily.",
            proposedText = "Daily, Using Excel.",
            sourceIds = listOf("exp-saffron-moved"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            decision = BulletDecision.PENDING,
            violations = emptyList(),
        )
        val state = stateFor(bullet)
        val item = ReviewFixtures.change(state, "t-case-only")

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
    }

    private fun stateFor(bullet: TailoredBullet): TailorUiState.Success {
        val extraEvidence = EvidenceBullet("exp-saffron-moved", bullet.originalText)
        val profile = ReviewFixtures.profile.copy(
            entries = ReviewFixtures.profile.entries.map { entry ->
                if (entry.id == "exp-saffron") {
                    entry.copy(bullets = entry.bullets + extraEvidence)
                } else {
                    entry
                }
            },
        )
        return buildTailorUiState(
            TailorInputs(
                application = ReviewFixtures.application(extra = listOf(bullet)),
                profile = profile,
                isOffline = false,
                regenerationsUsed = 0,
                editedBulletIds = emptySet(),
                reportedIds = emptySet(),
            ),
        ) as TailorUiState.Success
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
