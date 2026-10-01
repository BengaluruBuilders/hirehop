package com.hirehop.feature.applications.impl

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
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
    fun empty_readsInLightAndDark() = capture("ApplicationsEmpty", ApplicationsUiState.Empty)

    @Test
    fun list_readsInLightAndDark() = capture("ApplicationsList", previewListState())

    @Test
    fun offline_readsInLightAndDark() = capture("ApplicationsOffline", previewListState(isOffline = true))

    @Test
    fun syncPending_readsInLightAndDark() = capture(
        screenName = "ApplicationsSyncPending",
        uiState = ApplicationsUiState.Applications(
            rows = listOf(previewNorthwindRow(isSyncPending = true)) + previewListRows().drop(1),
            isOffline = false,
            statusSheet = null,
            message = null,
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
    fun empty_atLargeTextStacksAndWraps() = capture(
        screenName = "ApplicationsEmptyFont200",
        uiState = ApplicationsUiState.Empty,
        device = HhTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: ApplicationsUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                ApplicationsScreen(
                    uiState = uiState,
                    onAction = {},
                    credits = ApplicationCreditLine(1, ApplicationCreditUnit.Free),
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
