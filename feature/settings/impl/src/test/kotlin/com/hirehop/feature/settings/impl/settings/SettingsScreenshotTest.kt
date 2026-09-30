package com.hirehop.feature.settings.impl.settings

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
class SettingsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "SettingsDefault", uiState = defaultState())
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(screenName = "SettingsOffline", uiState = defaultState(isOffline = true))
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "SettingsDefaultFont200",
            uiState = defaultState(),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun offline_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "SettingsOfflineFont200",
            uiState = defaultState(isOffline = true),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: SettingsUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        showScreen(uiState)
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun showScreen(uiState: SettingsUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                SettingsScreen(
                    uiState = uiState,
                    actions = SettingsActions(),
                    versionName = null,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun defaultState(isOffline: Boolean = false): SettingsUiState = settingsStateFor(
        accountDisplayName = ACCOUNT_DISPLAY_NAME,
        creditsLeft = 4,
        isOffline = isOffline,
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        const val ACCOUNT_DISPLAY_NAME = "Priya Deshmukh"
    }
}
