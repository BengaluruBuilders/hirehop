package com.hirehop.feature.onboarding.impl.welcome

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
class WelcomeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun firstRun_readsInLightAndDark() {
        captureBothThemes(screenName = "WelcomeFirstRun", uiState = WelcomeUiState())
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeOffline",
            uiState = WelcomeUiState(isOffline = true),
        )
    }

    @Test
    fun loading_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeLoading",
            uiState = WelcomeUiState(isLoading = true),
        )
    }

    @Test
    fun loadFailed_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeLoadFailed",
            uiState = WelcomeUiState(message = WelcomeMessage.LOAD_FAILED),
        )
    }

    @Test
    fun heroBeforeAnyFact_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeHeroOriginal",
            uiState = WelcomeUiState(heroStage = WelcomeHeroStage.ORIGINAL),
        )
    }

    @Test
    fun heroRewritten_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeHeroRewritten",
            uiState = WelcomeUiState(heroStage = WelcomeHeroStage.REWRITTEN),
        )
    }

    @Test
    fun heroThreadDrawn_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeHeroThreadDrawn",
            uiState = WelcomeUiState(heroStage = WelcomeHeroStage.THREAD_DRAWN),
        )
    }

    @Test
    fun heroSettled_readsInLightAndDark() {
        captureBothThemes(
            screenName = "WelcomeHeroSettled",
            uiState = WelcomeUiState(heroStage = WelcomeHeroStage.SETTLED),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun firstRun_atLargeTextWrapsTheChips() {
        captureBothThemes(
            screenName = "WelcomeFirstRunFont200",
            uiState = WelcomeUiState(),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: WelcomeUiState,
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

    private fun showScreen(uiState: WelcomeUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                WelcomeScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = WelcomeActions(
        onPasteJobDescription = {},
        onImportResume = {},
        onBuildProfileStepByStep = {},
        onRetry = {},
        onDismissMessage = {},
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
    }
}
