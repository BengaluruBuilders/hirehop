package com.tailormyresume.feature.onboarding.impl.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.tailormyresume.core.designsystem.component.splitsAWord
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsActions
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsScenarioMapper
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsScreen
import com.tailormyresume.feature.onboarding.impl.consent.ConsentActions
import com.tailormyresume.feature.onboarding.impl.consent.ConsentScreen
import com.tailormyresume.feature.onboarding.impl.consent.ConsentUiState
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
@Config(qualifiers = "w230dp-h780dp-normal-long-notround-any-xhdpi-keyshidden-nonav", fontScale = 2f)
class OnboardingNarrowLargeFontTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val pasteActions = PasteJobDescriptionActions(
        onTextChange = {},
        onPaste = {},
        onCompanyChange = {},
        onRoleChange = {},
        onClear = {},
        onAnalyse = {},
        onRetry = {},
        onBack = {},
    )

    @Test
    fun welcome_keepsEveryWordWholeOrHyphenatedAndTheBrandUnsplit() {
        composeRule.setContent {
            TmrTheme {
                WelcomeScreen(
                    uiState = WelcomeUiState(),
                    actions = WelcomeActions({}, {}, {}, {}, {}),
                )
            }
        }

        assertNoRawBreakAndBrandUnsplit("TailorMyResume", "YOUR RESUME", "Get started")
    }

    @Test
    fun pasteJobDescription_keepsEveryWordWholeOrHyphenated() {
        composeRule.setContent {
            TmrTheme { PasteJobDescriptionScreen(uiState = PasteJobDescriptionUiState(text = JD), actions = pasteActions) }
        }

        assertNoRawBreakAndBrandUnsplit("PASTE THE JOB", "Clear", "Analyse")
    }

    @Test
    fun pasteJobDescription_clearKeepsItsIntrinsicWidth() {
        composeRule.setContent {
            TmrTheme { PasteJobDescriptionScreen(uiState = PasteJobDescriptionUiState(text = JD), actions = pasteActions) }
        }

        val clearLabel = textLayouts().first { it.layoutInput.text.text == "Clear" }
        val clearWidth = composeRule.onNodeWithText("Clear").getUnclippedBoundsInRoot().width
        val labelWidth = with(androidx.compose.ui.unit.Density(composeRule.activity.resources.displayMetrics.density, composeRule.activity.resources.configuration.fontScale)) {
            clearLabel.size.width.toDp()
        }

        assertThat(clearLabel.lineCount).isEqualTo(1)
        assertThat(clearWidth.value).isAtLeast((labelWidth + CLEAR_CHROME).value)
    }

    @Test
    fun pasteJobDescription_counterNeverOverlapsTheFieldText() {
        composeRule.setContent {
            TmrTheme { PasteJobDescriptionScreen(uiState = PasteJobDescriptionUiState(text = JD), actions = pasteActions) }
        }

        val counter = composeRule.onNode(hasContentDescription("words", substring = true), useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val field = composeRule.onNodeWithContentDescription("Job description", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val scrollers = composeRule.onAllNodes(hasScrollAction(), useUnmergedTree = true)
        val innermostScroller = (0 until scrollers.fetchSemanticsNodes().size)
            .map { scrollers[it].getUnclippedBoundsInRoot() }
            .filter { it.top <= field.top && it.bottom >= field.top }
            .minBy { it.bottom - it.top }
        val visibleTextBottom = minOf(field.bottom, innermostScroller.bottom)

        assertThat(counter.top.value).isAtLeast(visibleTextBottom.value)
    }

    @Test
    fun pasteJobDescription_discardDialogKeepsWordsWholeOrHyphenated() {
        composeRule.setContent {
            TmrTheme { PasteJobDescriptionScreen(uiState = PasteJobDescriptionUiState(text = JD), actions = pasteActions) }
        }
        composeRule.onNodeWithContentDescription("Go back").performClick()
        composeRule.waitForIdle()

        assertNoRawBreakAndBrandUnsplit("Discard this job description", "You typed text")
    }

    @Test
    @Config(qualifiers = "w200dp-h780dp-normal-long-notround-any-xhdpi-keyshidden-nonav")
    fun consentFirstRun_keepsEveryWordWholeOrHyphenated() {
        composeRule.setContent {
            TmrTheme { ConsentScreen(uiState = ConsentUiState(), actions = ConsentActions({}, {}, {}, {}, {})) }
        }

        assertNoRawBreakAndBrandUnsplit("NOTHING IS", "Continue")
    }

    @Test
    fun confirmFacts_keepsBrandWholeAndContinueButtonWide() {
        composeRule.setContent {
            TmrTheme {
                ConfirmFactsScreen(
                    uiState = ConfirmFactsScenarioMapper.withProfile(
                        state = ConfirmFactsScenarioMapper.seed(DebugScenario.PARTLY_CONFIRMED),
                        profile = sampleProfile,
                        scenario = DebugScenario.PARTLY_CONFIRMED,
                    ),
                    actions = ConfirmFactsActions({}, {}, { _, _ -> }, {}, {}, {}, {}),
                )
            }
        }

        assertNoRawBreakAndBrandUnsplit("TailorMyResume", "Continue to my analysis")
        val button = composeRule.onNodeWithText("Continue to my analysis", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val windowWidth = composeRule.activity.resources.configuration.screenWidthDp
        assertThat(button.width.value).isAtLeast(windowWidth * MIN_CONTINUE_WIDTH_SHARE)
        assertThat(button.bottom.value - button.top.value).isAtMost(MAX_CONTINUE_HEIGHT.value)
    }

    private fun textLayouts(): List<TextLayoutResult> =
        composeRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().flatMap { node ->
            mutableListOf<TextLayoutResult>().also { node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(it) }
        }

    private fun assertNoRawBreakAndBrandUnsplit(vararg keys: String) {
        val all = textLayouts()
        keys.forEach { key ->
            assertWithMessage("a text containing \"$key\"").that(all.any { it.layoutInput.text.text.contains(key) }).isTrue()
        }
        all.filter { layout -> keys.any { layout.layoutInput.text.text.contains(it) } }.forEach { layout ->
            val text = layout.layoutInput.text.text
            val isRaw = layout.splitsAWord() && layout.layoutInput.style.hyphens != Hyphens.Auto
            assertWithMessage("raw mid-word break in \"$text\"").that(isRaw).isFalse()
            assertWithMessage("brand split in \"$text\"").that(layout.splitsBrand()).isFalse()
        }
    }

    private fun TextLayoutResult.splitsBrand(): Boolean {
        val text = layoutInput.text.text
        val lineEnds = (0 until lineCount - 1).map(::getLineEnd)
        return Regex(BRAND).findAll(text).any { match -> lineEnds.any { it > match.range.first && it <= match.range.last } }
    }

    private companion object {
        const val BRAND = "TailorMyResume"
        val CLEAR_CHROME = 32.dp
        const val MIN_CONTINUE_WIDTH_SHARE = 0.65f
        val MAX_CONTINUE_HEIGHT = 140.dp

        val JD: String = "Globex Corporation is hiring a Senior Analyst, Business Intelligence, in Bengaluru. " +
            "You will build weekly reports in SQL and Advanced Excel, model dashboards in Power BI, " +
            "and report to stakeholders every Friday morning. Strong communication skills matter."
    }
}
