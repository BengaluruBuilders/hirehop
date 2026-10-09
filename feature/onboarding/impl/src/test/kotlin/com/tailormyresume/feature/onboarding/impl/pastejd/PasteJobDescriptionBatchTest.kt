package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class PasteJobDescriptionBatchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val state = mutableStateOf(PasteJobDescriptionUiState())
    private var analyseCount = 0
    private var backCount = 0

    private val actions = PasteJobDescriptionActions(
        onTextChange = {},
        onPaste = {},
        onCompanyChange = {},
        onRoleChange = {},
        onClear = {},
        onAnalyse = { analyseCount += 1 },
        onRetry = {},
        onBack = { backCount += 1 },
    )

    private val focusManager = object : FocusManager {
        var clearCount = 0

        override fun clearFocus(force: Boolean) {
            clearCount += 1
        }

        override fun moveFocus(focusDirection: FocusDirection): Boolean = false
    }

    private fun show(uiState: PasteJobDescriptionUiState) {
        state.value = uiState
        composeRule.setContent {
            CompositionLocalProvider(LocalFocusManager provides focusManager) {
                TmrTheme {
                    PasteJobDescriptionScreen(uiState = state.value, actions = actions)
                }
            }
        }
    }

    private fun fieldNode() = composeRule.onNodeWithContentDescription(FIELD_LABEL, useUnmergedTree = true)

    private fun assertDialogShown() {
        composeRule.onNodeWithText(DIALOG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DIALOG_BODY).assertIsDisplayed()
        composeRule.onNodeWithText(DISCARD).assertIsDisplayed()
        composeRule.onNodeWithText(KEEP_EDITING).assertIsDisplayed()
    }

    private fun tapBackControl() {
        composeRule.onNodeWithContentDescription(BACK).performClick()
    }

    private fun fieldHeightInDp(): Int = fieldNode().getBoundsInRoot().height.value.toInt()

    @Test
    fun dailyLimitKeepsAnalyseThisJdLabelDisabled() {
        show(PasteJobDescriptionUiState(text = SHORT_JD, freeAnalysesLeft = 0))

        composeRule.onNodeWithText(ANALYSE_LABEL).assertExists()
        composeRule.onNodeWithText(ANALYSE_LABEL).assertIsNotEnabled()
        composeRule.onAllNodesWithText(ANALYSE_LABEL_ALONE).assertCountEquals(0)
        composeRule.onNodeWithText(LIMIT_BANNER).assertIsDisplayed()
    }

    @Test
    fun longJdKeepsFieldHeightAndShowsCountAndClear() {
        show(PasteJobDescriptionUiState(text = LONG_JD, isOffline = true))

        composeRule.onNodeWithText(CLEAR_LABEL).assertIsDisplayed()
        composeRule.onNode(hasContentDescription(WORD_COUNT_PREFIX, substring = true)).assertIsDisplayed()
        assertTrue(fieldHeightInDp() <= 230)
    }

    @Test
    @Config(qualifiers = LANDSCAPE_QUALIFIERS)
    fun landscapeKeepsFieldHeight() {
        show(PasteJobDescriptionUiState(text = SHORT_JD))

        assertTrue(fieldHeightInDp() >= MIN_FIELD_AREA)
    }

    @Test
    fun portraitShortJdKeepsFieldHeight() {
        show(PasteJobDescriptionUiState(text = SHORT_JD))

        assertTrue(fieldHeightInDp() >= MIN_FIELD_AREA)
    }

    @Test
    fun pasteTapReleasesFieldFocus() {
        show(PasteJobDescriptionUiState())

        composeRule.onNodeWithText(PASTE_LABEL).performClick()

        assertEquals(1, focusManager.clearCount)
    }

    @Test
    fun analyseTapReleasesFieldFocus() {
        show(PasteJobDescriptionUiState(text = SHORT_JD, freeAnalysesLeft = 3))

        composeRule.onNodeWithText(ANALYSE_LABEL).performClick()

        assertEquals(1, analyseCount)
        assertEquals(1, focusManager.clearCount)
    }

    @Test
    fun backWithTextAsksBeforeLeaving() {
        show(PasteJobDescriptionUiState(text = SHORT_JD))

        tapBackControl()

        assertDialogShown()
        assertEquals(0, backCount)
    }

    @Test
    fun keepEditingStaysOnScreen() {
        show(PasteJobDescriptionUiState(text = SHORT_JD))
        tapBackControl()

        composeRule.onNodeWithText(KEEP_EDITING).performClick()

        composeRule.onNodeWithText(DIALOG_TITLE).assertDoesNotExist()
        assertEquals(0, backCount)
    }

    @Test
    fun discardLeaves() {
        show(PasteJobDescriptionUiState(text = SHORT_JD))
        tapBackControl()

        composeRule.onNodeWithText(DISCARD).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun backWithEmptyFieldLeavesAtOnce() {
        show(PasteJobDescriptionUiState())

        tapBackControl()

        assertEquals(1, backCount)
        composeRule.onNodeWithText(DIALOG_TITLE).assertDoesNotExist()
    }

    @Test
    fun systemBackWithTextAsksBeforeLeaving() {
        show(PasteJobDescriptionUiState(text = SHORT_JD))

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        composeRule.onNodeWithText(DIALOG_TITLE).assertIsDisplayed()
        assertEquals(0, backCount)
    }

    private companion object {
        const val LANDSCAPE_QUALIFIERS = "w851dp-h393dp-normal-long-notround-land-any-440dpi-keyshidden-nonav"
        const val MIN_FIELD_AREA = 90
        const val FIELD_LABEL = "Job description"
        const val BACK = "Go back"
        const val PASTE_LABEL = "Paste"
        const val CLEAR_LABEL = "Clear"
        const val ANALYSE_LABEL = "Analyse this JD"
        const val ANALYSE_LABEL_ALONE = "Analyse"
        const val DIALOG_TITLE = "Discard this job description?"
        const val DIALOG_BODY = "You typed text that is not analysed. If you go back, the text is lost."
        const val DISCARD = "Discard"
        const val KEEP_EDITING = "Keep editing"
        const val WORD_COUNT_PREFIX = "Words pasted so far"
        const val LIMIT_BANNER =
            "You've used today's 3 free analyses. You get 3 more tomorrow."

        val SHORT_JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, model dashboards in Power BI, and report to stakeholders every Friday morning."

        val LONG_JD: String = LONG_JD_SENTENCE.repeat(LONG_JD_REPEATS).trim()

        const val LONG_JD_REPEATS = 10
        const val LONG_JD_SENTENCE =
            "We are hiring an analyst who turns messy numbers into a clear weekly story for the " +
                "leadership team and who explains every model in plain words. "
    }
}
