package com.tailormyresume.feature.analysis.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class AnalysisGapNameTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sentence = "You will build screens with Kotlin and SQL in a modular codebase."

    private val gap = RequirementItem(
        requirement = JobRequirement(
            id = "req-1",
            text = sentence,
            type = RequirementType.SKILL,
            priority = RequirementPriority.MUST_HAVE,
            keywords = listOf("kotlin", "sql"),
        ),
        status = MatchStatus.GAP,
        skills = emptyList(),
        isInPrepPlan = false,
    )

    private fun showResult() {
        val state = AnalysisUiState.Result(
            job = JobLabel("Android Developer", "Acme"),
            keywordCoverage = KeywordCoverage(0, 2),
            sections = listOf(RequirementSection(RequirementGroup.MustHaveGaps, listOf(gap))),
            totalCredits = 1,
        )
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }
    }

    @Test
    fun gapCardUsesTheSkillNamesAndShowsTheSentenceAsAskedFor() {
        showResult()

        composeRule.onNodeWithText("Kotlin, SQL").assertExists()
        composeRule.onNodeWithText("Asked for: $sentence").assertExists()
    }

    @Test
    fun longRequirementWithBracketShowsTheWholeSentenceAsAskedFor() {
        val bracketed = "Experience building dashboards for business users across regions (Power BI or Tableau)"
        val item = gap.copy(requirement = gap.requirement.copy(text = bracketed, keywords = listOf("power bi", "tableau")))
        val state = AnalysisUiState.Result(
            job = JobLabel("Android Developer", "Acme"),
            keywordCoverage = KeywordCoverage(0, 2),
            sections = listOf(RequirementSection(RequirementGroup.MustHaveGaps, listOf(item))),
            totalCredits = 1,
        )
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }

        composeRule.onNodeWithText("Power BI, Tableau").assertExists()
        composeRule.onNodeWithText("Asked for: $bracketed").assertExists()
    }

    @Test
    fun questionSheetAsksAboutTheSkillName() {
        composeRule.setContent { TmrTheme { QuestionSheetContent(gap, AnalysisActions()) } }

        composeRule.onNodeWithText("Where have you used Kotlin, SQL?").assertExists()
        composeRule.onNodeWithText("Must-have · Kotlin, SQL").assertExists()
    }

    @Test
    fun notClosedMessageListsTheKeywords() {
        composeRule.setContent { TmrTheme { QuestionSheetContent(gap, AnalysisActions(), notClosed = true) } }

        composeRule.onNodeWithText(
            "Your words do not mention Kotlin or SQL, so this gap stays open and nothing was saved. " +
                "Say where you used it, in your own words.",
        ).assertExists()
    }
}
