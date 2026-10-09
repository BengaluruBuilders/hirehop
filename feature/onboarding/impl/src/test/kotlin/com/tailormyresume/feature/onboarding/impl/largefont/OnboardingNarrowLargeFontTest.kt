package com.tailormyresume.feature.onboarding.impl.largefont

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.Hyphens
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsActions
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsScenarioMapper
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsScreen
import com.tailormyresume.feature.onboarding.impl.consent.ConsentActions
import com.tailormyresume.feature.onboarding.impl.consent.ConsentScreen
import com.tailormyresume.feature.onboarding.impl.consent.ConsentUiState
import com.tailormyresume.feature.onboarding.impl.importresume.ImportResumeActions
import com.tailormyresume.feature.onboarding.impl.importresume.ImportResumeScenarioMapper
import com.tailormyresume.feature.onboarding.impl.importresume.ImportResumeScreen
import com.tailormyresume.feature.onboarding.impl.pastejd.PasteJobDescriptionActions
import com.tailormyresume.feature.onboarding.impl.pastejd.PasteJobDescriptionScreen
import com.tailormyresume.feature.onboarding.impl.pastejd.PasteJobDescriptionUiState
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeActions
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeScreen
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val EMULATOR_QUALIFIERS = "w230dp-h914dp-normal-long-notround-any-500dpi-keyshidden-nonav"
private const val LONG_WORD_LENGTH = 8
private const val MIN_BEFORE_BREAK = 2
private const val MIN_AFTER_BREAK = 3
private const val MAX_FOOTER_SHARE = 0.4f
private const val MIN_LIST_SHARE = 0.3f

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = EMULATOR_QUALIFIERS, fontScale = 2.0f)
class OnboardingNarrowLargeFontTest {

    @get:org.junit.Rule
    val composeRule = createComposeRule()

    @Test
    fun welcomeHeadingNeverBreaksMidWordWithoutAHyphen() {
        composeRule.setContent {
            TmrTheme { WelcomeScreen(uiState = WelcomeUiState(), actions = welcomeActions) }
        }
        composeRule.waitForIdle()

        assertNoRawMidWordBreaks { it.startsWith("YOUR RESUME") || it.contains("never invents") || it == "TailorMyResume" }
    }

    @Test
    fun pasteJdKeepsTheNoteWholeTheClearPillOnOneLineAndTheCounterOffTheText() {
        composeRule.setContent {
            TmrTheme { PasteJobDescriptionScreen(uiState = PasteJobDescriptionUiState(text = JD), actions = pasteActions) }
        }
        composeRule.waitForIdle()

        val focus = { text: String -> text.startsWith("PASTE THE") || text == "Clear" || text.startsWith("Your JD stays") }
        assertNoVisualOverflow(focus)
        assertNoRawMidWordBreaks(focus)
        assertEquals(1, layoutOf { it.layoutInput.text.text == "Clear" }.lineCount)
        val counter = composeRule.onNodeWithContentDescription("Words pasted so far", substring = true, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val textArea = composeRule.onNodeWithContentDescription("Job description", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        assertTrue("counter $counter overlaps text $textArea", counter.top >= textArea.bottom - 0.5f)
    }

    @Test
    fun consentWrapsAtWordBoundariesAndNeverBreaksTheHeadingWithoutAHyphen() {
        composeRule.setContent {
            TmrTheme { ConsentScreen(uiState = ConsentUiState(), actions = consentActions) }
        }
        composeRule.waitForIdle()

        val focus = { text: String ->
            text.startsWith("NOTHING IS") || text == "I understand" || text.startsWith("Tick each") || text.endsWith("understood")
        }
        assertNoVisualOverflow { text -> focus(text) && text != "I understand" }
        assertNoRawMidWordBreaks(focus)
        val buttons = layoutsOf().filter { it.layoutInput.text.text == "I understand" }
        assertTrue("no I understand label found", buttons.isNotEmpty())
        assertTrue(buttons.all { it.lineCount == 1 })
    }

    @Test
    fun importResumeKeepsFileNameWithinTwoLinesAndOtherTextWhole() {
        val state = ImportResumeScenarioMapper.seed(DebugScenario.SUCCESS).copy(fileName = LONG_FILE_NAME)
        composeRule.setContent {
            TmrTheme { ImportResumeScreen(uiState = state, actions = importActions) }
        }
        composeRule.waitForIdle()

        val name = layoutOf { it.layoutInput.text.text == LONG_FILE_NAME }
        assertTrue(name.lineCount <= 2)
        val focus = { text: String -> text != LONG_FILE_NAME && (text.startsWith("ADD YOUR") || text.startsWith("Nothing is used")) }
        assertNoVisualOverflow(focus)
        assertNoRawMidWordBreaks(focus)
    }

    @Test
    fun confirmFactsKeepsTheListVisibleAndTheFooterSmall() {
        val state = ConfirmFactsScenarioMapper.withProfile(
            state = ConfirmFactsScenarioMapper.seed(DebugScenario.DEFAULT),
            profile = PROFILE,
            scenario = DebugScenario.DEFAULT,
        )
        composeRule.setContent {
            TmrTheme { ConfirmFactsScreen(uiState = state, actions = confirmActions) }
        }
        composeRule.waitForIdle()

        val focus = { text: String -> text.startsWith("We removed") || text.contains("not confirmed") || (text.startsWith("Continue") && text != "Continued") }
        assertNoVisualOverflow(focus)
        assertNoRawMidWordBreaks(focus)
        val root = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val scroller = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().maxBy { it.boundsInRoot.height }
        val footer = root.bottom - scroller.boundsInRoot.bottom
        assertTrue("footer $footer of ${root.height}", footer <= root.height * MAX_FOOTER_SHARE)
        assertTrue(scroller.boundsInRoot.height >= root.height * MIN_LIST_SHARE)
        composeRule.onNodeWithText("left to review", substring = true).assertExists()
        val continueBottom = composeRule.onNodeWithText("left to review", substring = true)
            .fetchSemanticsNode().boundsInRoot.bottom
        assertTrue(continueBottom <= root.bottom)
    }

    private fun layoutsOf(): List<TextLayoutResult> = composeRule
        .onAllNodes(
            SemanticsMatcher("has text layout") { it.config.contains(SemanticsActions.GetTextLayoutResult) },
            useUnmergedTree = true,
        )
        .fetchSemanticsNodes()
        .mapNotNull(::layoutOfNode)

    private fun layoutOfNode(node: SemanticsNode): TextLayoutResult? {
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.firstOrNull()
    }

    private fun layoutOf(predicate: (TextLayoutResult) -> Boolean): TextLayoutResult =
        layoutsOf().first(predicate)

    private fun focused(focus: (String) -> Boolean): List<TextLayoutResult> {
        val matched = layoutsOf().filter { focus(it.layoutInput.text.text) }
        assertTrue("no text matched the focus", matched.isNotEmpty())
        return matched
    }

    private fun assertNoVisualOverflow(focus: (String) -> Boolean) {
        val clipped = focused(focus).filter { it.hasVisualOverflow }
        assertTrue(
            "clipped text: " + clipped.joinToString { it.layoutInput.text.text.take(40) },
            clipped.isEmpty(),
        )
    }

    private fun assertNoRawMidWordBreaks(focus: (String) -> Boolean) {
        val offenders = focused(focus)
            .flatMap { layout -> midWordBreaks(layout).map { split -> layout to split } }
            .filter { (layout, split) -> layout.layoutInput.style.hyphens != Hyphens.Auto || !split.isHyphenatable() }
        assertTrue(
            "raw mid-word breaks: " + offenders.joinToString { (layout, split) ->
                "${split.word}@${split.before} in ${layout.layoutInput.text.text.take(30)}"
            },
            offenders.isEmpty(),
        )
    }

    private data class WordSplit(val word: String, val before: Int) {
        fun isHyphenatable() = word.length >= LONG_WORD_LENGTH && before >= MIN_BEFORE_BREAK && word.length - before >= MIN_AFTER_BREAK
    }

    private fun midWordBreaks(layout: TextLayoutResult): List<WordSplit> {
        val text = layout.layoutInput.text.text
        return (0 until layout.lineCount - 1).mapNotNull { line ->
            val end = layout.getLineEnd(line, visibleEnd = false)
            if (end in 1 until text.length && !text[end - 1].isWhitespace() && !text[end].isWhitespace() && text[end - 1] != '-') {
                val start = text.substring(0, end).lastIndexOfAny(charArrayOf(' ', '\n')) + 1
                val stop = text.indexOfAny(charArrayOf(' ', '\n'), end).let { if (it < 0) text.length else it }
                WordSplit(text.substring(start, stop), end - start)
            } else {
                null
            }
        }
    }

    private val welcomeActions = WelcomeActions(
        onPasteJobDescription = {},
        onSelectCareerStage = {},
        onHaveAccount = {},
        onRetry = {},
        onDismissMessage = {},
    )

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

    private val consentActions = ConsentActions(
        onPurposeToggle = {},
        onAgree = {},
        onNotNow = {},
        onReadAgain = {},
        onBack = {},
    )

    private val importActions = ImportResumeActions(
        onBack = {},
        onPickFile = {},
        onChooseAnotherFile = {},
        onRetry = {},
        onStartGuidedForm = {},
        onReviewFacts = {},
    )

    private val confirmActions = ConfirmFactsActions(
        onBack = {},
        onConfirm = {},
        onEdit = { _, _ -> },
        onAddOne = {},
        onSkip = {},
        onContinue = {},
        onImportResume = {},
    )

    private companion object {
        const val LONG_FILE_NAME = "Priya_Deshmukh_Resume_Final.pdf"

        const val JD = "Senior Android Engineer at Northwind. You will build weekly reports in Kotlin and " +
            "Compose, and report to stakeholders every Friday morning with a short written summary."

        val PROFILE = CandidateProfile(
            fullName = "Asha Rao",
            email = "asha.rao@example.com",
            phone = "+91 90000 00000",
            headline = "",
            skills = listOf("SQL", "Power BI"),
            entries = (1..4).map { index ->
                ProfileEntry(
                    id = "W-0$index",
                    category = EntryCategory.EXPERIENCE,
                    title = "Data Operations Associate, Saffron Retail",
                    organization = "Pune",
                    startDate = "Jul 2025",
                    endDate = "now",
                    bullets = listOf(EvidenceBullet(id = "W-0$index-b1", text = "Built weekly sales reports in Excel.")),
                    source = FactSource.IMPORTED,
                    isConfirmed = false,
                )
            },
        )
    }
}
