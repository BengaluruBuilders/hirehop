package com.tailormyresume.feature.analysis.impl

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class AnalysisInteractionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()

    private val actions = AnalysisActions(
        onTailor = { calls += "tailor" },
        onOpenShareCard = { calls += "share" },
        onIHaveThis = { calls += "ihave:$it" },
        onTogglePrepPlan = { calls += "prep:$it" },
        onOpenMenu = { calls += "menu:$it" },
        onSeeSource = { calls += "source:$it" },
        onRetry = { calls += "retry" },
        onBackToJobDescription = { calls += "backToJob" },
        onBackClick = { calls += "back" },
        onUndo = { calls += "undo" },
    )

    private fun show(state: AnalysisUiState) {
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = actions) } }
    }

    private fun resultWith(vararg items: RequirementItem) = AnalysisUiState.Result(
        job = JobLabel("Associate Analyst", "Northwind GCC"),
        keywordCoverage = KeywordCoverage(1, 2),
        sections = listOf(
            RequirementSection(RequirementGroup.MustHaveGaps, items.filter { it.isGap }),
            RequirementSection(RequirementGroup.Met, items.filter { !it.isGap }),
        ).filter { it.items.isNotEmpty() },
        totalCredits = 1,
    )

    private val submitted = mutableListOf<String>()
    private val shared = mutableListOf<String>()

    private val gap = resultItem("req-a", "Cloud data warehouse", MatchStatus.GAP)
    private val met = resultItem("req-b", "SQL", MatchStatus.MET, factId = "W-01")

    @Test
    fun gapRow_sendsItsActions() {
        show(resultWith(gap, met))

        composeRule.onNodeWithText("I have this").performClick()
        composeRule.onNodeWithText("Add to my prep plan").performClick()

        assertThat(calls).containsExactly("ihave:req-a", "prep:req-a").inOrder()
    }

    @Test
    fun metRow_opensTheSourceFromTheRow() {
        show(resultWith(gap, met))

        composeRule.onNodeWithText("W-01").performScrollTo().performClick()

        assertThat(calls).containsExactly("source:req-b")
    }

    @Test
    fun overflowButton_opensTheRowMenu() {
        show(resultWith(met))

        composeRule.onAllNodesWithContentDescription("More actions").onFirst().performClick()

        assertThat(calls).containsExactly("menu:req-b")
    }

    @Test
    fun header_sharesAndBar_tailors() {
        show(resultWith(met))

        composeRule.onNodeWithContentDescription("Share my fit").performClick()
        composeRule.onNodeWithText("Tailor my resume").performClick()

        assertThat(calls).containsExactly("share", "tailor").inOrder()
    }

    @Test
    fun tailor_isOffInOfflineAndFreeLimitStates() {
        show(resultWith(met).copy(isOffline = true))
        composeRule.onNodeWithText("Tailor when I'm back").assertIsNotEnabled()
        composeRule.onNodeWithText("Tailor needs a connection.").assertDoesNotExist()
    }

    @Test
    fun result_saysTheCountIsNotAScore() {
        show(resultWith(met))

        composeRule.onNodeWithText("You cover 1 of 2 key terms. This is not a score.").assertExists()
    }

    @Test
    fun tailor_isOnInTheNormalState() {
        show(resultWith(met))

        composeRule.onNodeWithText("Tailor my resume").assertIsEnabled()
    }

    @Test
    fun prepToast_offersUndo() {
        show(resultWith(gap).copy(toast = AnalysisToast.PrepAdded("req-a", "Cloud data warehouse")))

        composeRule.onNodeWithText("Cloud data warehouse added to your prep plan").assertExists()
        composeRule.onNodeWithText("Undo").performClick()

        assertThat(calls).containsExactly("undo")
    }

    @Test
    fun reportToast_thanksThePersonAndHasNoUndo() {
        show(resultWith(met).copy(toast = AnalysisToast.Reported))

        composeRule.onNodeWithText("Thanks. We will check this.").assertExists()
        composeRule.onNodeWithText("Undo").assertDoesNotExist()
    }

    @Test
    fun tailorLimit_showsTheLimitNoticeAndKeepsTailorOff() {
        show(resultWith(met).copy(tailorLimitReached = true))

        composeRule.onNodeWithText("You've used today's free tailoring.", substring = true).assertExists()
        composeRule.onNodeWithText("Tailor my resume").assertIsNotEnabled()
    }

    @Test
    fun blankRoleAndCompany_useTheFallbackText() {
        show(resultWith(met).copy(job = JobLabel()))

        composeRule.onNodeWithText("Role not set · Company not set").assertExists()
    }

    @Test
    fun reportedRowWithoutASource_hasNoMenuButton() {
        show(resultWith(gap.copy(isReported = true)))

        composeRule.onAllNodesWithContentDescription("More actions").assertCountEquals(0)
    }

    @Test
    fun errorState_retries() {
        show(AnalysisUiState.Failed(JobLabel("Associate Analyst", "Northwind GCC")))

        composeRule.onNodeWithText("Try again").performClick()

        assertThat(calls).containsExactly("retry")
    }

    @Test
    fun dailyLimit_returnsToTheJob() {
        show(AnalysisUiState.DailyLimit(JobLabel("Associate Analyst", "Northwind GCC")))

        composeRule.onNodeWithText("Back to my JD").performClick()

        assertThat(calls).containsExactly("backToJob")
    }

    @Test
    fun quotaReached_returnsToTheJobInsteadOfRetrying() {
        show(AnalysisUiState.Failed(JobLabel("Associate Analyst", "Northwind GCC"), FailureCause.QuotaReached))

        composeRule.onAllNodesWithText("Try again").assertCountEquals(0)
        composeRule.onNodeWithText("Back to my JD").performClick()

        assertThat(calls).containsExactly("backToJob")
    }

    @Test
    fun rateLimited_showsAWaitInsideTheBounds() {
        show(AnalysisUiState.Failed(JobLabel("Associate Analyst", "Northwind GCC"), FailureCause.RateLimited(30)))

        composeRule.onNodeWithText("Please wait 30 seconds, then tap Try again.").assertExists()
    }

    @Test
    fun rateLimited_dropsAWaitOutsideTheBounds() {
        val state = androidx.compose.runtime.mutableStateOf<AnalysisUiState>(
            AnalysisUiState.Failed(JobLabel("Associate Analyst", "Northwind GCC"), FailureCause.RateLimited(0)),
        )
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state.value, actions = actions) } }

        listOf(0, -5, 3601, Int.MAX_VALUE).forEach { seconds ->
            state.value = AnalysisUiState.Failed(JobLabel("Associate Analyst", "Northwind GCC"), FailureCause.RateLimited(seconds))
            composeRule.waitForIdle()

            composeRule.onNodeWithText("Please wait a little, then tap Try again.").assertExists()
        }
    }

    @Test
    fun header_backWorks() {
        show(resultWith(met))

        composeRule.onNodeWithContentDescription("Go back").performClick()

        assertThat(calls).containsExactly("back")
    }

    @Test
    fun questionSheet_writeStaysOffUntilTheFactIsTypedThenSubmitsIt() {
        composeRule.setContent {
            TmrTheme {
                QuestionSheetContent(gap, AnalysisActions(onSubmitEvidence = { id, text -> submitted += "$id:$text" }))
            }
        }

        composeRule.onNodeWithText("Where have you used Cloud data warehouse?").assertExists()
        composeRule.onNodeWithText("Save as new fact").assertIsNotEnabled()
        composeRule.onNode(hasSetTextAction()).performTextInput("Built a BigQuery warehouse")
        composeRule.onNodeWithText("Save as new fact").assertIsEnabled().performClick()

        assertThat(submitted).containsExactly("req-a:Built a BigQuery warehouse")
    }

    @Test
    fun shareScreen_sharesTheRoleAndCountsAndNeverTheCompany() {
        composeRule.setContent {
            TmrTheme {
                ShareFitScreen(
                    state = resultWith(gap, met),
                    actions = AnalysisActions(onShareText = { shared += it }),
                )
            }
        }

        composeRule.onNodeWithText("Only the JD fit is shared. No name, contact details or resume facts.").assertExists()
        composeRule.onNodeWithText("Share my fit").performClick()

        assertThat(shared).containsExactly(
            "My fit for Associate Analyst: 1 met, 1 to prepare. Made with TailorMyResume.",
        )
    }
}

private fun resultItem(
    id: String,
    text: String,
    status: MatchStatus,
    factId: String? = null,
) = RequirementItem(
    requirement = com.tailormyresume.core.model.JobRequirement(
        id = id,
        text = text,
        type = com.tailormyresume.core.model.RequirementType.SKILL,
        priority = com.tailormyresume.core.model.RequirementPriority.MUST_HAVE,
        keywords = listOf(text.lowercase()),
    ),
    status = status,
    skills = emptyList(),
    isInPrepPlan = false,
    factRefs = listOfNotNull(
        factId?.let {
            RequirementFactRef(
                factId = it,
                displayId = it,
                title = "Entry",
                organization = "",
                startDate = "",
                endDate = "",
                lines = emptyList(),
                source = com.tailormyresume.core.model.FactSource.IMPORTED,
                isConfirmed = true,
            )
        },
    ),
)
