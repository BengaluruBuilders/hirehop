package com.tailormyresume.feature.tailor.impl

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class BulletReviewKeptWordsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun acceptedOrders_legendDoesNotListWordsAlreadyWritten() {
        val state = ReviewFixtures.state()
        val item = ReviewFixtures.change(state, ReviewFixtures.ORDERS)

        composeRule.setContent {
            TmrTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 2,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        composeRule.onNode(hasText("Added words")).assertExists()
        composeRule.onNode(hasText("Cleaned")).assertExists()
        assertThat(composeRule.onAllNodes(hasText("SQL", substring = false)).fetchSemanticsNodes()).hasSize(KEYWORD_CHIP_ONLY)
    }

    @Test
    fun mergedSources_legendDoesNotListExcel() {
        val state = ReviewFixtures.state()
        val item = ReviewFixtures.change(state, ReviewFixtures.INTERN)

        composeRule.setContent {
            TmrTheme {
                BulletReviewSheetContent(
                    item = item,
                    position = 3,
                    total = state.totalCount,
                    actions = emptyActions(),
                )
            }
        }

        composeRule.onNode(hasText("Added words")).assertExists()
        assertThat(composeRule.onAllNodes(hasText("Excel", substring = false)).fetchSemanticsNodes()).hasSize(KEYWORD_CHIP_ONLY)
    }

    private companion object {
        const val KEYWORD_CHIP_ONLY = 1
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
