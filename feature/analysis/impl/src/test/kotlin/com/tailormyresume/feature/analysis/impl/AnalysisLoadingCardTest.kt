package com.tailormyresume.feature.analysis.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class AnalysisLoadingCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(state: AnalysisUiState) {
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }
    }

    @Test
    fun loadingDoesNotShowTheFallbackJobTitle() {
        show(AnalysisUiState.Loading)

        composeRule.onNodeWithText("Job analysis").assertDoesNotExist()
    }

    @Test
    fun loadingStillShowsTheWaitingSteps() {
        show(AnalysisUiState.Loading)

        composeRule.onNodeWithText("Reading the JD").assertExists()
    }

    @Test
    fun analyzingWithBlankLabelsKeepsTheTruthfulFallback() {
        show(AnalysisUiState.Analyzing(job = JobLabel(), factCount = 3))

        composeRule.onNodeWithText("Job analysis").assertExists()
    }
}
