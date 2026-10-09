package com.tailormyresume.feature.settings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevice
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import com.tailormyresume.feature.settings.impl.deleteaccount.DeleteAccountActions
import com.tailormyresume.feature.settings.impl.deleteaccount.DeleteAccountScreen
import com.tailormyresume.feature.settings.impl.deleteaccount.DeleteAccountUiState
import com.tailormyresume.feature.settings.impl.settings.SettingsActions
import com.tailormyresume.feature.settings.impl.settings.SettingsScreen
import com.tailormyresume.feature.settings.impl.settings.SettingsUiState
import com.tailormyresume.feature.settings.impl.yourdata.YourDataActions
import com.tailormyresume.feature.settings.impl.yourdata.YourDataScreen
import com.tailormyresume.feature.settings.impl.yourdata.YourDataUiState
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class SettingsLoadingScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun settingsLoading_readsInLightAndDark() {
        captureBothThemes(screenName = "SettingsLoading") {
            SettingsScreen(uiState = SettingsUiState.Loading, actions = settingsNoActions)
        }
    }

    @Test
    fun yourDataLoading_readsInLightAndDark() {
        captureBothThemes(screenName = "YourDataLoading") {
            YourDataScreen(uiState = YourDataUiState.Loading, actions = yourDataNoActions)
        }
    }

    @Test
    fun deleteAccountLoading_readsInLightAndDark() {
        captureBothThemes(screenName = "DeleteAccountLoading") {
            DeleteAccountScreen(uiState = DeleteAccountUiState.Loading, actions = deleteAccountNoActions)
        }
    }

    private fun captureBothThemes(
        screenName: String,
        device: TmrTestDevice = TmrTestDevices.board,
        content: @Composable () -> Unit,
    ) = runBlocking {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) { content() }
        }
        composeRule.onNodeWithTag("loadingWheel").assertExists().assertIsDisplayed()
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private val yourDataNoActions = YourDataActions(
        onBack = {},
        onViewProfile = {},
        onCorrectProfile = {},
        onViewApplications = {},
        onViewPurchases = {},
        onDownload = {},
        onDeleteRequest = {},
        onDeleteConfirm = {},
        onDeleteDismiss = {},
        onDeleteMyData = {},
        onDeleteMyDataConfirm = {},
        onDeleteMyDataDismiss = {},
    )

    private val deleteAccountNoActions = DeleteAccountActions(
        onBack = {},
        onKeepAccount = {},
        onDeleteAccount = {},
        onDeleteConfirmed = {},
        onDeleteDismissed = {},
        onDownloadData = {},
    )

    private val settingsNoActions = SettingsActions(
        onSignIn = {},
        onSignOut = {},
        onSignOutConfirm = {},
        onSignOutDismiss = {},
        onCreditsAndHelp = {},
        onYourData = {},
        onConsentNotice = {},
        onDeleteAccount = {},
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
    }
}
