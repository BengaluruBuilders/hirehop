package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.component.TmrHeaderCollapseState
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
class ApplicationsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun empty_readsInLightAndDark() = capture("ApplicationsEmpty", ApplicationsUiState.Empty(PREVIEW_HEADER))

    @Test
    fun list_readsInLightAndDark() = capture("ApplicationsList", previewListState())

    @Test
    fun offline_readsInLightAndDark() = capture("ApplicationsOffline", previewListState(isOffline = true))

    @Test
    fun syncPending_readsInLightAndDark() = capture(
        screenName = "ApplicationsSyncPending",
        uiState = ApplicationsUiState.Applications(
            header = PREVIEW_HEADER,
            rows = listOf(previewPaisaRow().copy(isSyncPending = true)) +
                previewListRows().filterNot { row -> row.id == previewPaisaRow().id },
            isOffline = false,
            statusSheet = null,
            message = null,
        ),
    )

    @Test
    fun scrolled_collapsesTheHeaderToTheCompactBar() = capture(
        screenName = "ApplicationsScrolled",
        uiState = previewListState().let { state ->
            state.copy(rows = state.rows + state.rows.map { row -> row.copy(id = row.id + "-more") })
        },
        firstVisibleItem = 1,
    )

    @Test
    fun blankRoleAndCompany_showTheFallbackWordsAndANeutralMonogram() = capture(
        screenName = "ApplicationsBlankFields",
        uiState = previewListState().let { state ->
            state.copy(rows = state.rows.map { row -> if (row.id == previewPaisaRow().id) row.copy(role = "", company = "") else row })
        },
    )

    @Test
    fun message_showsTheStatusToastWithUndo() = capture(
        screenName = "ApplicationsStatusToast",
        uiState = previewListState(
            message = ApplicationStatusMessage(status = ApplicationStatus.INTERVIEW, canUndo = true),
        ),
    )

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun list_atLargeTextStacksAndWraps() = capture(
        screenName = "ApplicationsListFont200",
        uiState = previewListState(),
        device = TmrTestDevices.boardLargeFont,
    )

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun oneRow_atLargeTextKeepsTheHeadingWhole() = capture(
        screenName = "ApplicationsOneRowFont200",
        uiState = previewListState().let { state -> state.copy(rows = listOf(previewPaisaRow())) },
        device = TmrTestDevices.boardLargeFont,
    )

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun empty_atLargeTextStacksAndWraps() = capture(
        screenName = "ApplicationsEmptyFont200",
        uiState = ApplicationsUiState.Empty(PREVIEW_HEADER),
        device = TmrTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: ApplicationsUiState,
        device: TmrTestDevice = TmrTestDevices.board,
        firstVisibleItem: Int = 0,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                ApplicationsScreen(
                    uiState = uiState,
                    onAction = {},
                    listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleItem),
                    collapse = remember { TmrHeaderCollapseState(initialFraction = if (firstVisibleItem > 0) 1f else 0f) },
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

internal const val TRACKED_OUTPUT_DIR = "src/test/screenshots"
