package com.tailormyresume.feature.profile.impl.evidencepath

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h800dp")
class EvidencePathHeaderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val longTitle =
        "Library database project that tracks every borrowed book, every late fee and every reader across three campuses"

    private val card = EvidenceFactCard(
        EvidenceCategory.PROJECTS,
        ProfileEntry(
            id = "P-01",
            category = EntryCategory.PROJECT,
            title = longTitle,
            organization = "",
            startDate = "",
            endDate = "",
            bullets = emptyList(),
            source = FactSource.USER_STATED,
            isConfirmed = true,
        ),
    )

    @Test
    fun longQuestionHeaderStaysOnOneLine() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                EvidencePathScreen(
                    uiState = EvidencePathUiState(
                        category = EvidenceCategory.PROJECTS,
                        questionIndex = 1,
                        cards = listOf(card),
                        anchor = card,
                    ),
                    actions = EvidencePathActions.None,
                    onBack = {},
                )
            }
        }

        val node = composeRule.onNode(hasText("Question 2 of 4", substring = true)).fetchSemanticsNode()
        val layouts = mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        assertThat(layouts.single().lineCount == 1 && layouts.single().isLineEllipsized(0)).isTrue()
    }

    @Test
    fun allDoneTitleReadsNewFactsWithoutAdded() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                EvidencePathScreen(
                    uiState = EvidencePathUiState(isDone = true, cards = listOf(card, card.copy())),
                    actions = EvidencePathActions.None,
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("2 new facts", ignoreCase = true).assertExists()
    }
}
