package com.tailormyresume.feature.onboarding.impl.common

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertWithMessage
import com.tailormyresume.core.designsystem.component.splitsAWord
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.feature.onboarding.impl.pastejd.PasteJobDescriptionActions
import com.tailormyresume.feature.onboarding.impl.pastejd.PasteJobDescriptionScreen
import com.tailormyresume.feature.onboarding.impl.pastejd.PasteJobDescriptionUiState
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeActions
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeScreen
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.SMALL_PHONE_QUALIFIERS, fontScale = TmrTestDevices.LARGE_FONT_SCALE)
class OnboardingWholeWordsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun welcome_wordmarkHeadlineAndConfirmedChipKeepWholeWords() {
        composeRule.setContent {
            TmrTheme {
                WelcomeScreen(
                    uiState = WelcomeUiState(),
                    actions = WelcomeActions(
                        onPasteJobDescription = {},
                        onSelectCareerStage = {},
                        onHaveAccount = {},
                        onRetry = {},
                        onDismissMessage = {},
                    ),
                )
            }
        }

        assertKeepsWholeWords(
            listOf(
                "wordmark" to composeRule.onNodeWithText("TailorMyResume", useUnmergedTree = true),
                "headline" to composeRule.onNode(hasText(WELCOME_HEADLINE), useUnmergedTree = true),
                "confirmed chip" to composeRule.onNodeWithText("Confirmed", useUnmergedTree = true),
            ),
        )
    }

    @Test
    fun pasteJobDescription_titleAndClearKeepWholeWords() {
        composeRule.setContent {
            TmrTheme {
                Box(Modifier.width(NARROW_PHONE_WIDTH)) {
                    PasteJobDescriptionScreen(
                        uiState = PasteJobDescriptionUiState(text = JD),
                        actions = PasteJobDescriptionActions(
                            onTextChange = {},
                            onPaste = {},
                            onCompanyChange = {},
                            onRoleChange = {},
                            onClear = {},
                            onAnalyse = {},
                            onRetry = {},
                            onBack = {},
                        ),
                    )
                }
            }
        }

        assertKeepsWholeWords(
            listOf(
                "PASTE THE JOB DESCRIPTION" to composeRule.onNode(hasText("PASTE THE JOB DESCRIPTION"), useUnmergedTree = true),
                "Clear" to composeRule.onNodeWithText("Clear", useUnmergedTree = true),
            ),
        )
    }

    private fun assertKeepsWholeWords(texts: List<Pair<String, SemanticsNodeInteraction>>) {
        texts.forEach { (name, node) ->
            val layouts = mutableListOf<TextLayoutResult>()
            node.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
            assertWithMessage("$name has a text layout").that(layouts).isNotEmpty()
            assertWithMessage("$name breaks inside a word").that(layouts.first().splitsAWord()).isFalse()
        }
    }

    private companion object {
        val NARROW_PHONE_WIDTH = 290.dp

        const val WELCOME_HEADLINE = "YOUR RESUME, REWRITTEN FROM YOUR FACTS"

        val JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, model dashboards in Power BI, and report to stakeholders every Friday morning."
    }
}
