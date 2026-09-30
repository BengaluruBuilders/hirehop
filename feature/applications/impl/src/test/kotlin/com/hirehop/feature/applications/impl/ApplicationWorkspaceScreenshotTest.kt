package com.hirehop.feature.applications.impl

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ApplicationWorkspaceScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun full_readsInLightAndDark() = capture("WorkspaceFull", previewWorkspaceReadyState())

    @Test
    fun fullScrolled_showsOnePrepTaskPerGapWithATickAndTheReportAffordance() = capture(
        screenName = "WorkspaceFullScrolled",
        uiState = previewWorkspaceReadyState(
            prepTasks = listOf(
                previewPrepTask(id = "req-agile", isDone = false, isOverflowOpen = true),
                previewPrepTask(id = "req-communication", isDone = true, isOverflowOpen = false),
            ),
        ),
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
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun full_atLargeTextStacksAndWraps() = capture(
        screenName = "WorkspaceFullFont200",
        uiState = previewWorkspaceReadyState(),
        device = HhTestDevices.boardLargeFont,
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun notExportedYet_atLargeTextStacksAndWraps() = capture(
        screenName = "WorkspaceNotExportedYetFont200",
        uiState = previewWorkspaceReadyState(
            status = ApplicationStatus.SAVED,
            resume = WorkspaceResume.NotExported,
        ),
        device = HhTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: ApplicationDetailUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                ApplicationDetailScreen(
                    uiState = uiState,
                    onAction = {},
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
