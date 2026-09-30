package com.hirehop.feature.onboarding.impl.consent

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
class ConsentScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "ConsentDefault", uiState = baseState())
    }

    @Test
    fun partAcknowledged_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ConsentPartAcknowledged",
            uiState = baseState(acknowledged = setOf(ConsentPurpose.READ_AND_BUILD)),
        )
    }

    @Test
    fun allAcknowledged_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ConsentAllAcknowledged",
            uiState = baseState(acknowledged = ConsentPurpose.entries.toSet()),
        )
    }

    @Test
    fun declined_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ConsentDeclined",
            uiState = baseState(isDeclined = true),
        )
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ConsentOffline",
            uiState = baseState(isOffline = true),
        )
    }

    @Test
    fun saving_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ConsentSaving",
            uiState = baseState(isSaving = true),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "ConsentDefaultFont200",
            uiState = baseState(),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    @Config(qualifiers = HhTestDevices.SMALL_PHONE_QUALIFIERS)
    fun allAcknowledged_onASmallPhone() {
        captureBothThemes(
            screenName = "ConsentAllAcknowledgedSmallPhone",
            uiState = baseState(acknowledged = ConsentPurpose.entries.toSet()),
            device = HhTestDevices.smallPhone,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: ConsentUiState,
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

    private fun showScreen(uiState: ConsentUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                ConsentScreen(
                    uiState = uiState,
                    actions = noOpActions,
                    onSkipToJobDescription = {},
                )
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = ConsentActions(
        onPurposeToggle = { },
        onAgree = {},
        onNotNow = {},
        onReadAgain = {},
    )

    private fun baseState(
        acknowledged: Set<ConsentPurpose> = emptySet(),
        isSaving: Boolean = false,
        isOffline: Boolean = false,
        isDeclined: Boolean = false,
    ) = ConsentUiState(
        entries = consentPurposeStates().map { it.copy(isAcknowledged = it.purpose in acknowledged) },
        isSaving = isSaving,
        isOffline = isOffline,
        isDeclined = isDeclined,
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
    }
}
