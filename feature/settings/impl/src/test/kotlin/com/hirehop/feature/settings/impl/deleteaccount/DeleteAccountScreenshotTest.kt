package com.hirehop.feature.settings.impl.deleteaccount

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.account.AccountDeletionCounts
import com.hirehop.core.model.DebugScenario
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
class DeleteAccountScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountDefault", scenario = DebugScenario.DEFAULT)
    }

    @Test
    fun deleting_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountDeleting", scenario = DebugScenario.DELETING)
    }

    @Test
    fun done_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountDone", scenario = DebugScenario.SUCCESS)
    }

    @Test
    fun error_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountError", scenario = DebugScenario.ERROR)
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountOffline", scenario = DebugScenario.OFFLINE)
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "DeleteAccountDefaultFont200",
            scenario = DebugScenario.DEFAULT,
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        scenario: DebugScenario,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        val uiState = deleteAccountStateFor(scenario = scenario, counts = DESIGN_COUNTS)
        showScreen(uiState)
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun showScreen(uiState: DeleteAccountUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                DeleteAccountScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = DeleteAccountActions(
        onBack = {},
        onKeepAccount = {},
        onDeleteAccount = {},
        onDownloadData = {},
        onBackToWelcome = {},
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"

        val DESIGN_COUNTS = AccountDeletionCounts(
            profileFacts = 18,
            applications = 4,
            unusedCredits = 4,
        )
    }
}
