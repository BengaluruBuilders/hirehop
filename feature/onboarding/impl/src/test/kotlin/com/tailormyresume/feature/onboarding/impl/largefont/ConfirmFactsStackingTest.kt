package com.tailormyresume.feature.onboarding.impl.largefont

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE_337_QUALIFIERS = "w337dp-h914dp-normal-long-notround-any-500dpi-keyshidden-nonav"
private const val PHONE_393_QUALIFIERS = "w393dp-h851dp-normal-long-notround-any-420dpi-keyshidden-nonav"

private val NO_OP_ACTIONS = ConfirmFactsActions(
    onBack = {},
    onConfirm = {},
    onEdit = { _, _ -> },
    onAddOne = {},
    onSkip = {},
    onContinue = {},
    onImportResume = {},
)

private val PENDING_PROFILE = CandidateProfile(
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

private fun ComposeContentTestRule.showPendingFacts() {
    val state = ConfirmFactsScenarioMapper.withProfile(
        state = ConfirmFactsScenarioMapper.seed(DebugScenario.DEFAULT),
        profile = PENDING_PROFILE,
        scenario = DebugScenario.DEFAULT,
    )
    setContent { TmrTheme { ConfirmFactsScreen(uiState = state, actions = NO_OP_ACTIONS) } }
    waitForIdle()
}

private fun ComposeContentTestRule.layoutsWithText(label: String): List<TextLayoutResult> = onAllNodes(
    SemanticsMatcher("has text layout") { it.config.contains(SemanticsActions.GetTextLayoutResult) },
    useUnmergedTree = true,
)
    .fetchSemanticsNodes()
    .mapNotNull { node ->
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        results.firstOrNull()
    }
    .filter { it.layoutInput.text.text == label }

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE_337_QUALIFIERS, fontScale = 2.0f)
class ConfirmFactsStackedFactCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pendingChipConfirmEditAndSkipStayOnWholeSingleLines() {
        composeRule.showPendingFacts()

        listOf("Pending", "Confirm", "Edit", "Skip").forEach { label ->
            val layouts = composeRule.layoutsWithText(label)
            assertTrue("no $label label found", layouts.isNotEmpty())
            assertTrue("$label wrapped", layouts.all { it.lineCount == 1 })
        }
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = PHONE_393_QUALIFIERS, fontScale = 1.0f)
class ConfirmFactsNormalFontNoticeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reasonNoticeSitsInTheBottomBarAboveContinueAndOutsideTheScrollingList() {
        composeRule.showPendingFacts()

        val reason = composeRule.onNodeWithText("Continue is off until you confirm at least one item below.")
            .fetchSemanticsNode().boundsInRoot
        val scroller = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().maxBy { it.boundsInRoot.height }
        val action = composeRule.onNodeWithText("left to review", substring = true).fetchSemanticsNode().boundsInRoot
        assertTrue("reason $reason is inside the list ${scroller.boundsInRoot}", reason.top >= scroller.boundsInRoot.bottom - 0.5f)
        assertTrue("reason $reason is not above the bar action $action", reason.bottom <= action.top + 0.5f)
        assertEquals(0, composeRule.layoutsWithText("Continue is off until you confirm at least one item.").size)
    }
}
