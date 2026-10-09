package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertWithMessage
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
class ApplicationWorkspaceRequirementSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun gapRow_tapOpensTheSheet_portrait() {
        val actions = renderWorkspace(previewWorkspaceReadyState(isGapExpanded = true))

        composeRule.onNodeWithText("BigQuery").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertWithMessage("the gap row asks for its own sheet")
            .that(actions)
            .contains(ApplicationWorkspaceAction.RequirementChosen("req-bigquery"))
    }

    @Test
    @Config(qualifiers = "w852dp-h393dp-normal-long-notround-any-440dpi-keyshidden-nonav")
    fun gapRow_tapOpensTheSheet_landscape() {
        val actions = renderWorkspace(previewWorkspaceReadyState(isGapExpanded = true))

        composeRule.onNodeWithText("BigQuery").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertWithMessage("the gap row asks for its own sheet in landscape too")
            .that(actions)
            .contains(ApplicationWorkspaceAction.RequirementChosen("req-bigquery"))
    }

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun gapRow_tapOpensSheetWithAskedForAndStatus() {
        renderWorkspace(
            previewWorkspaceReadyState(
                isGapExpanded = true,
                requirementSheet = gapRequirementSheet(),
            ),
        )

        composeRule.onNodeWithText("Asked for: Must have BigQuery for large datasets").assertIsDisplayed()
        composeRule.onNodeWithText("Add to my prep plan").assertIsDisplayed()
        assertWithMessage("the sheet names the status of the requirement")
            .that(composeRule.onAllNodesWithText("To prepare").fetchSemanticsNodes().isNotEmpty())
            .isTrue()
    }

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun metRow_listsEvidenceFactIds() {
        renderWorkspace(
            previewWorkspaceReadyState(
                isGapExpanded = true,
                requirementSheet = metRequirementSheet(),
            ),
        )

        composeRule.onNodeWithText("S-02").assertIsDisplayed()
        composeRule.onNodeWithText("W-01").assertIsDisplayed()
        composeRule.onNodeWithText("Add to my prep plan").assertDoesNotExist()
        assertWithMessage("the sheet names the status of the requirement")
            .that(composeRule.onAllNodesWithText("Met").fetchSemanticsNodes().isNotEmpty())
            .isTrue()
    }

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun gapRow_listsNoEvidenceFactIds() {
        renderWorkspace(
            previewWorkspaceReadyState(
                isGapExpanded = true,
                requirementSheet = gapRequirementSheet(),
            ),
        )

        composeRule.onNodeWithText("Facts that match").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun addToPrepPlan_sendsTheActionAndThenReadsInYourPrepPlan() {
        val actions = renderWorkspace(
            previewWorkspaceReadyState(
                isGapExpanded = true,
                requirementSheet = gapRequirementSheet(),
            ),
        )

        composeRule.onNodeWithText("Add to my prep plan").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertWithMessage("the sheet asks to add the requirement to the prep plan")
            .that(actions)
            .contains(ApplicationWorkspaceAction.RequirementPrepAddChosen("req-bigquery"))

        composeRule.runOnUiThread {
            state.value = previewWorkspaceReadyState(
                isGapExpanded = true,
                requirementSheet = gapRequirementSheet(isInPrepPlan = true),
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("In your prep plan").assertIsDisplayed().assertIsNotEnabled()
        composeRule.onNodeWithText("Add to my prep plan").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun row_isAButtonWithRequirementAndStatusDescription() {
        renderWorkspace(previewWorkspaceReadyState(isGapExpanded = true))

        composeRule.onNodeWithContentDescription("BigQuery. To prepare")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button))
    }

    @Test
    @Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
    fun sheetTitle_isAHeading() {
        renderWorkspace(
            previewWorkspaceReadyState(
                isGapExpanded = true,
                requirementSheet = gapRequirementSheet(),
            ),
        )

        val headings = composeRule.onAllNodesWithText("BigQuery").fetchSemanticsNodes()
            .filter { node -> SemanticsProperties.Heading in node.config }

        assertWithMessage("the sheet title is a heading")
            .that(headings.isNotEmpty())
            .isTrue()
    }

    private val state: MutableState<ApplicationDetailUiState> = mutableStateOf(
        previewWorkspaceReadyState(isGapExpanded = true),
    )

    private fun renderWorkspace(uiState: ApplicationDetailUiState): List<ApplicationWorkspaceAction> {
        val actions = mutableListOf<ApplicationWorkspaceAction>()
        composeRule.runOnUiThread { state.value = uiState }
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationDetailScreen(
                    uiState = state.value,
                    onAction = { actions += it },
                    scrollState = rememberScrollState(),
                    now = PREVIEW_INSTANT,
                )
            }
        }
        return actions
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationWorkspaceRequirementSheetScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun requirementSheetGap_readsInLightAndDark() = capture(
        screenName = "WorkspaceRequirementSheetGap",
        uiState = previewWorkspaceReadyState(
            isGapExpanded = true,
            requirementSheet = gapRequirementSheet(),
        ),
    )

    @Test
    fun requirementSheetMet_readsInLightAndDark() = capture(
        screenName = "WorkspaceRequirementSheetMet",
        uiState = previewWorkspaceReadyState(
            isGapExpanded = true,
            requirementSheet = metRequirementSheet(),
        ),
    )

    private fun capture(
        screenName: String,
        uiState: ApplicationDetailUiState,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                ApplicationDetailScreen(
                    uiState = uiState,
                    onAction = {},
                    scrollState = rememberScrollState(),
                    now = PREVIEW_INSTANT,
                )
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, TmrTestDevices.board) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private fun gapRequirementSheet(isInPrepPlan: Boolean = false) = WorkspaceRequirementSheetState(
    id = "req-bigquery",
    name = "BigQuery",
    requirementText = "Must have BigQuery for large datasets",
    status = MatchStatus.GAP,
    evidenceIds = emptyList(),
    isInPrepPlan = isInPrepPlan,
)

private fun metRequirementSheet() = WorkspaceRequirementSheetState(
    id = "req-sql",
    name = "SQL for reporting",
    requirementText = "SQL for reporting",
    status = MatchStatus.MET,
    evidenceIds = listOf("S-02", "W-01"),
    isInPrepPlan = false,
)
