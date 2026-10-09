package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmFactsProgressSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun progressBarExposesConfirmedOverTotal() {
        composeRule.setContent { TmrTheme(darkTheme = false) { ProgressBar(confirmed = 1, total = 4) } }

        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(0.25f, 0f..1f),
                ),
            )
    }

    @Test
    fun progressBarWithNoFactsIsEmptyNotNaN() {
        composeRule.setContent { TmrTheme(darkTheme = false) { ProgressBar(confirmed = 0, total = 0) } }

        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(0f, 0f..1f),
                ),
            )
    }
}
