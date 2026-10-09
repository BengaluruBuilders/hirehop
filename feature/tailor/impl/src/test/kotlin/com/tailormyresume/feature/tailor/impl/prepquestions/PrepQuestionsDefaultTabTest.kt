package com.tailormyresume.feature.tailor.impl.prepquestions

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.prep.PrepQuestionKind
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class PrepQuestionsDefaultTabTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = PrepQuestionsActions(
        onReportInaccurate = {},
        onDismissMessage = {},
        onRetry = {},
        onOpenPrepPlan = {},
        onEditFact = { _, _ -> },
        onNavigateBack = {},
    )

    private val strength = PrepQuestionCard(
        id = "q1",
        kind = PrepQuestionKind.STRENGTH,
        ordinal = 1,
        prompt = "Tell me about your SQL work.",
        requirementText = "Experience with SQL.",
        fact = null,
    )

    private val gap = strength.copy(
        id = "q2",
        kind = PrepQuestionKind.GAP,
        ordinal = 2,
        prompt = "How would you handle a missing skill?",
    )

    @Test
    fun factsTabIsSelectedOnceQuestionsLoad() {
        val uiState = mutableStateOf(PrepQuestionsUiState(stage = PrepQuestionsStage.GENERATING))
        composeRule.setContent {
            TmrTheme {
                PrepQuestionsScreen(uiState = uiState.value, actions = actions)
            }
        }

        uiState.value = PrepQuestionsUiState(
            stage = PrepQuestionsStage.READY,
            jobCompany = "Northwind GCC",
            groups = listOf(
                PrepQuestionGroup(kind = PrepQuestionKind.STRENGTH, cards = listOf(strength)),
                PrepQuestionGroup(kind = PrepQuestionKind.GAP, cards = listOf(gap)),
            ),
        )
        composeRule.waitForIdle()

        assertThat(composeRule.onAllNodes(isSelectable()).fetchSemanticsNodes()).hasSize(2)
        composeRule.onNode(hasText("From your facts", substring = true) and isSelectable()).assertIsSelected()
    }
}
