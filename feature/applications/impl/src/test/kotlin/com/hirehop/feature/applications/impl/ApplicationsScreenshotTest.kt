package com.hirehop.feature.applications.impl

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.component.HhHeaderCollapseState
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
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun list_atLargeTextStacksAndWraps() = capture(
        screenName = "ApplicationsListFont200",
        uiState = previewListState(),
        device = HhTestDevices.boardLargeFont,
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun oneRow_atLargeTextKeepsTheHeadingWhole() = capture(
        screenName = "ApplicationsOneRowFont200",
        uiState = previewListState().let { state -> state.copy(rows = listOf(previewPaisaRow())) },
        device = HhTestDevices.boardLargeFont,
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun empty_atLargeTextStacksAndWraps() = capture(
        screenName = "ApplicationsEmptyFont200",
        uiState = ApplicationsUiState.Empty(PREVIEW_HEADER),
        device = HhTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: ApplicationsUiState,
        device: HhTestDevice = HhTestDevices.board,
        firstVisibleItem: Int = 0,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                ApplicationsScreen(
                    uiState = uiState,
                    onAction = {},
                    listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleItem),
                    collapse = remember { HhHeaderCollapseState(initialFraction = if (firstVisibleItem > 0) 1f else 0f) },
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
