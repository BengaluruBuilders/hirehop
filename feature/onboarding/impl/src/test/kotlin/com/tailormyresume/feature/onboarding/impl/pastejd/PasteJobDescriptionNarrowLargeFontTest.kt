package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val NARROW_QUALIFIERS = "w320dp-h480dp-normal-long-notround-any-xhdpi-keyshidden-nonav"

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = NARROW_QUALIFIERS, fontScale = TmrTestDevices.LARGE_FONT_SCALE)
class PasteJobDescriptionNarrowLargeFontTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val actions = PasteJobDescriptionActions(
        onTextChange = {},
        onPaste = {},
        onCompanyChange = {},
        onRoleChange = {},
        onClear = {},
        onAnalyse = {},
        onRetry = {},
        onBack = {},
    )

    private fun show(uiState: PasteJobDescriptionUiState) {
        composeRule.setContent {
            TmrTheme { PasteJobDescriptionScreen(uiState = uiState, actions = actions) }
        }
    }

    private fun fieldBounds(): DpRect =
        composeRule.onNodeWithContentDescription(FIELD_LABEL, useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun viewportBottom(): Dp {
        val scrollers = composeRule.onAllNodes(hasScrollAction(), useUnmergedTree = true)
        val count = scrollers.fetchSemanticsNodes().size
        return (0 until count).map { scrollers[it].getUnclippedBoundsInRoot() }.maxBy { it.height }.bottom
    }

    @Test
    fun pastedFieldKeepsMinimumHeightAndIsDisplayed() {
        show(PasteJobDescriptionUiState(text = JD))

        composeRule.onNodeWithContentDescription(FIELD_LABEL, useUnmergedTree = true).assertIsDisplayed()
        assertTrue(fieldBounds().height >= MIN_FIELD_HEIGHT)
        assertTrue(minOf(fieldBounds().bottom, viewportBottom()) - fieldBounds().top >= MIN_FIELD_HEIGHT)
    }

    @Test
    fun pastedFieldKeepsClearInsideTheVisibleArea() {
        show(PasteJobDescriptionUiState(text = JD))

        val clear = composeRule.onNodeWithText(CLEAR_LABEL)
        clear.assertIsDisplayed()
        assertTrue(clear.getUnclippedBoundsInRoot().bottom <= viewportBottom())
    }

    @Test
    fun emptyFieldKeepsMinimumHeightInsideTheVisibleArea() {
        show(PasteJobDescriptionUiState())

        assertTrue(fieldBounds().height >= MIN_FIELD_HEIGHT)
        assertTrue(minOf(fieldBounds().bottom, viewportBottom()) - fieldBounds().top >= MIN_FIELD_HEIGHT)
    }

    private companion object {
        const val FIELD_LABEL = "Job description"
        const val CLEAR_LABEL = "Clear"
        val MIN_FIELD_HEIGHT = 120.dp

        val JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, model dashboards in Power BI, and report to stakeholders every Friday morning."
    }
}
