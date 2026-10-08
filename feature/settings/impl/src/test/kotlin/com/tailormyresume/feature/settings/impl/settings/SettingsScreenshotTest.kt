package com.tailormyresume.feature.settings.impl.settings

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.screenshot.TmrTestDevice
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class SettingsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "SettingsDefault", uiState = content())
    }

    @Test
    fun signOutConfirm_readsInLightAndDark() {
        captureBothThemes(screenName = "SettingsSignOutConfirm", uiState = content(isSignOutConfirmVisible = true))
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(screenName = "SettingsOffline", uiState = content(isOffline = true))
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeText() {
        captureBothThemes(
            screenName = "SettingsDefaultFont200",
            uiState = content(),
            device = TmrTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: SettingsUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = runBlocking {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                SettingsScreen(uiState = uiState, actions = noActions, versionName = VERSION)
            }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun content(
        isOffline: Boolean = false,
        isSignOutConfirmVisible: Boolean = false,
    ) = SettingsUiState.Content(
        account = SignInAccount.localAccount,
        creditsLeft = CREDITS,
        consentAcceptedAt = CONSENT_TIME,
        isOffline = isOffline,
        isSignOutConfirmVisible = isSignOutConfirmVisible,
    )

    private val noActions = SettingsActions(
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
        const val CREDITS = 4
        const val VERSION = "1.0 (beta)"
        val CONSENT_TIME = Instant.fromEpochSeconds(1_802_606_400)
    }
}
