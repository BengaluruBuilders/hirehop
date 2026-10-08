package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.screenshot.TmrTestDevice
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationWorkspaceScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun full_readsInLightAndDark() = capture("WorkspaceFull", previewWorkspaceReadyState())

    @Test
    fun blankRoleAndCompany_showTheFallbackWordsInTheHeader() = capture(
        screenName = "WorkspaceBlankFields",
        uiState = previewWorkspaceReadyState().copy(jobTitle = "", company = ""),
    )

    @Test
    fun exportedWithPageCountAndTemplate_listsThemInTheResumeCard() = capture(
        screenName = "WorkspaceExportDetail",
        uiState = previewWorkspaceReadyState(
            resume = previewExportedResume().copy(pageCount = 1, templateName = "Plain"),
        ),
    )

    @Test
    fun fullScrolled_showsThePrepPlanNotesJobDescriptionAndEntries() = capture(
        screenName = "WorkspaceFullScrolled",
        uiState = previewWorkspaceReadyState(),
        scrollTo = SCROLLED_PX,
    )

    @Test
    fun gapExpanded_listsEveryRequirementWithItsStatusWord() = capture(
        screenName = "WorkspaceGapExpanded",
        uiState = previewWorkspaceReadyState(isGapExpanded = true),
    )

    @Test
    fun reportedTask_thanksThePerson() = capture(
        screenName = "WorkspaceTaskReported",
        uiState = previewWorkspaceReadyState(
            prepTasks = listOf(
                previewPrepTask(id = "req-bigquery", isDone = true),
                previewPrepTask(id = "req-agile", isDone = false, isReported = true),
                previewPrepTask(id = "req-python", isDone = false),
            ),
        ),
        scrollTo = SCROLLED_PX,
    )

    @Test
    fun notExportedYet_offersThePreviewAndShowsNoPrice() = capture(
        screenName = "WorkspaceNotExportedYet",
        uiState = previewWorkspaceReadyState(
            status = ApplicationStatus.SAVED,
            resume = WorkspaceResume.NotExported,
        ),
    )

    @Test
    fun offlineRead_readsEverywhere() = capture(
        screenName = "WorkspaceOfflineRead",
        uiState = previewWorkspaceReadyState(isOffline = true),
    )

    @Test
    fun notesSaved_saysJustNowAndKeepsTheJobDescriptionCollapsedUntilTapped() = capture(
        screenName = "WorkspaceNotesSaved",
        uiState = previewWorkspaceReadyState(
            notesState = WorkspaceNotesState.SavedJustNow,
            isNotesFocused = true,
        ),
    )

    @Test
    fun deleteDialog_statesExactlyWhatGoesAndWhatStays() = capture(
        screenName = "WorkspaceDeleteDialog",
        uiState = previewWorkspaceReadyState(isDeleteDialogVisible = true),
    )

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun full_atLargeTextStacksAndWraps() = capture(
        screenName = "WorkspaceFullFont200",
        uiState = previewWorkspaceReadyState(),
        device = TmrTestDevices.boardLargeFont,
    )

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun notExportedYet_atLargeTextStacksAndWraps() = capture(
        screenName = "WorkspaceNotExportedYetFont200",
        uiState = previewWorkspaceReadyState(
            status = ApplicationStatus.SAVED,
            resume = WorkspaceResume.NotExported,
        ),
        device = TmrTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: ApplicationDetailUiState,
        device: TmrTestDevice = TmrTestDevices.board,
        scrollTo: Int = 0,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                ApplicationDetailScreen(
                    uiState = uiState,
                    onAction = {},
                    scrollState = rememberScrollState(initial = scrollTo),
                    now = PREVIEW_INSTANT,
                )
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private const val SCROLLED_PX = 900
