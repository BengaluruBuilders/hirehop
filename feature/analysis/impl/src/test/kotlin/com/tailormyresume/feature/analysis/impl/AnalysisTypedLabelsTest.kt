package com.tailormyresume.feature.analysis.impl

import androidx.compose.ui.test.junit4.createComposeRule
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
class AnalysisTypedLabelsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val typedLabels = JobLabel("Platform Analyst", "Contoso Labs")

    private fun result(job: JobLabel = typedLabels) = AnalysisUiState.Result(
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
    fun typedLabelsShowOnTheWaitingCard() {
        showWaiting(AnalysisUiState.Analyzing(job = typedLabels, factCount = 3))

        composeRule.onNodeWithText("Platform Analyst").assertExists()
        composeRule.onNodeWithText("Contoso Labs").assertExists()
        composeRule.onNodeWithText("Job analysis").assertDoesNotExist()
    }

    @Test
    fun typedLabelsShowOnTheResultCaption() {
        showResult(result())

        composeRule.onNodeWithText("Platform Analyst · Contoso Labs").assertExists()
        composeRule.onNodeWithText("Role not set · Company not set").assertDoesNotExist()
    }

    @Test
    fun typedLabelsShowOnTheShareCard() {
        showShare(result())

        composeRule.onNodeWithText("Contoso Labs").assertExists()
        composeRule.onNodeWithText("Role not set").assertDoesNotExist()
    }
}
