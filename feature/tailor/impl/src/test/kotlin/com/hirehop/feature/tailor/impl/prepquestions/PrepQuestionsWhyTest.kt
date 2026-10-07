package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.core.screenshot.HhTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class PrepQuestionsWhyTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun whyLine_endsWithOnePeriodAndGapHeaderIsSingular() {
        val strength = PrepQuestionCard(
            id = "q1",
            kind = PrepQuestionKind.STRENGTH,
            ordinal = 1,
            prompt = "Tell me about your SQL work.",
            requirementText = "Experience with SQL.",
            fact = null,
        )
        val gap = strength.copy(
            id = "q2",
            kind = PrepQuestionKind.GAP,
            ordinal = 2,
            prompt = "How would you handle a missing skill?",
        )
        composeRule.setContent {
            HhTheme {
                PrepQuestionsScreen(
                    uiState = PrepQuestionsUiState(
                        stage = PrepQuestionsStage.READY,
                        jobCompany = "Northwind GCC",
                        groups = listOf(
                            PrepQuestionGroup(kind = PrepQuestionKind.STRENGTH, cards = listOf(strength)),
                            PrepQuestionGroup(kind = PrepQuestionKind.GAP, cards = listOf(gap)),
                        ),
                    ),
                    actions = PrepQuestionsActions(
                        onReportInaccurate = {},
                        onDismissMessage = {},
                        onRetry = {},
                        onOpenPrepPlan = {},
                        onEditFact = { _, _ -> },
                        onNavigateBack = {},
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Why they may ask: the JD asks for Experience with SQL.").assertExists()
        composeRule.onAllNodesWithText("..", substring = true).assertCountEquals(0)
        composeRule.onNodeWithText("Your gaps").assertExists()
    }
}
