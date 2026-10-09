package com.tailormyresume.feature.analysis.impl

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class AnalysisLabelFillTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val seniorDeveloper = JobLabel("Senior Android Developer", "Northwind Mobile")

    private fun result(job: JobLabel = seniorDeveloper) = AnalysisUiState.Result(
        job = job,
        keywordCoverage = KeywordCoverage(1, 2),
        sections = emptyList(),
        totalCredits = 1,
    )

    private fun showWaiting(state: AnalysisUiState.Analyzing) {
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }
    }

    private fun showResult(state: AnalysisUiState.Result) {
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }
    }

    private fun showShare(state: AnalysisUiState.Result) {
        composeRule.setContent { TmrTheme { ShareFitScreen(state = state, actions = AnalysisActions()) } }
    }

    @Test
    fun waitingCardShowsRoleCompanyAndInitial() {
        showWaiting(AnalysisUiState.Analyzing(job = seniorDeveloper, factCount = 3))

        composeRule.onNodeWithText("Senior Android Developer").assertExists()
        composeRule.onNodeWithText("Northwind Mobile").assertExists()
        composeRule.onNodeWithText("N", useUnmergedTree = true).assertExists()
    }

    @Test
    fun waitingCardKeepsFallbackWhenUnknown() {
        showWaiting(AnalysisUiState.Analyzing(job = JobLabel(), factCount = 3))

        composeRule.onNodeWithText("Job analysis").assertExists()
        composeRule.onNodeWithText("Role not set").assertDoesNotExist()
    }

    @Test
    fun resultCaptionJoinsRoleAndCompany() {
        showResult(result())

        composeRule.onNodeWithText("Senior Android Developer · Northwind Mobile").assertExists()
    }

    @Test
    fun resultCaptionShowsNotSetOnlyWhenEmpty() {
        showResult(result(job = JobLabel()))

        composeRule.onNodeWithText("Role not set · Company not set").assertExists()
    }

    @Test
    fun shareCardShowsCompanyLine() {
        showShare(result())

        composeRule.onNodeWithText("Northwind Mobile").assertExists()
    }

    @Test
    fun shareCardOmitsCompanyLineWhenUnknown() {
        showShare(result(job = JobLabel(title = "Senior Android Developer")))

        composeRule.onAllNodesWithText("Northwind Mobile").assertCountEquals(0)
        composeRule.onNodeWithText("Company not set").assertDoesNotExist()
    }
}
