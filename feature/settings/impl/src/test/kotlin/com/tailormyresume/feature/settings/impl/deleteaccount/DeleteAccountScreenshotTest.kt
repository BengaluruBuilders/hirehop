package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.account.AccountDeletionCounts
import com.tailormyresume.core.domain.account.AccountDeletionStep
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
class DeleteAccountScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountDefault", uiState = ready())
    }

    @Test
    fun deleting_readsInLightAndDark() {
        captureBothThemes(
            screenName = "DeleteAccountDeleting",
            uiState = DeleteAccountUiState.Deleting(
                counts = COUNTS,
                accountEmail = EMAIL,
                step = AccountDeletionStep.DELETING_PROFILE_FACTS,
            ),
        )
    }

    @Test
    fun done_readsInLightAndDark() {
        capture(screenName = "DeleteAccountDone") { AccountDeletedScreen(onDone = {}) }
    }

    @Test
    fun error_readsInLightAndDark() {
        captureBothThemes(
            screenName = "DeleteAccountError",
            uiState = ready(failure = DeleteAccountFailure.DATA_INTACT),
        )
    }

    @Test
    fun partlyDeleted_readsInLightAndDark() {
        captureBothThemes(
            screenName = "DeleteAccountPartlyDeleted",
            uiState = ready(failure = DeleteAccountFailure.PARTLY_DELETED),
        )
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountOffline", uiState = ready(isOffline = true))
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeText() {
        captureBothThemes(
            screenName = "DeleteAccountDefaultFont200",
            uiState = ready(),
            device = TmrTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: DeleteAccountUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = capture(screenName, device) { DeleteAccountScreen(uiState = uiState, actions = noActions) }

    private fun capture(
        screenName: String,
        device: TmrTestDevice = TmrTestDevices.board,
        content: @Composable () -> Unit,
    ) = runBlocking {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) { content() }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun ready(
        isOffline: Boolean = false,
        failure: DeleteAccountFailure? = null,
    ) = DeleteAccountUiState.Ready(
        counts = COUNTS,
        accountEmail = EMAIL,
        isOffline = isOffline,
        failure = failure,
    )

    private val noActions = DeleteAccountActions(
        onBack = {},
        onKeepAccount = {},
        onDeleteAccount = {},
        onDownloadData = {},
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        const val EMAIL = "priya.d@example.com"
        val COUNTS = AccountDeletionCounts(profileFacts = 18, applications = 4, unusedCredits = 4)
    }
}
