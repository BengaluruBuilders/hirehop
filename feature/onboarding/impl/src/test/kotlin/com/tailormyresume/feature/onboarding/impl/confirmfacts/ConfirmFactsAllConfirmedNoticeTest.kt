package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.data.sampleProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class ConfirmFactsAllConfirmedNoticeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun withSkills_noticeNamesThem() {
        show(skills = listOf("SQL", "Power BI"))

        composeRule.onNodeWithText("TailorMyResume uses only these and your skills.", substring = true).assertExists()
    }

    @Test
    fun withoutSkills_noticeSaysOnlyTheFacts() {
        show(skills = emptyList())

        composeRule.onNodeWithText("TailorMyResume uses only these.", substring = true).assertExists()
    }

    private fun show(skills: List<String>) {
        val scenario = DebugScenario.FULLY_CONFIRMED
        val state = ConfirmFactsScenarioMapper.withProfile(
            state = ConfirmFactsScenarioMapper.seed(scenario),
            profile = sampleProfile.copy(skills = skills),
            scenario = scenario,
        )
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ConfirmFactsScreen(
                    uiState = state,
                    actions = ConfirmFactsActions(
                        onBack = {},
                        onConfirm = {},
                        onEdit = { _, _ -> },
                        onAddOne = {},
                        onSkip = {},
                        onContinue = {},
                        onImportResume = {},
                    ),
                )
            }
        }
        composeRule.waitForIdle()
    }
}
