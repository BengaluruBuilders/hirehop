package com.tailormyresume.feature.onboarding.impl.consent

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ConsentPurpose
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
    fun readOnly_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ConsentReadOnly",
            uiState = baseState(
                acknowledged = ConsentPurpose.entries.toSet(),
                isReadOnly = true,
                agreedAt = Instant.fromEpochSeconds(1_800_000_000),
            ),
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
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "ConsentDefaultFont200",
            uiState = baseState(),
            device = TmrTestDevices.boardLargeFont,
        )
    }

    @Test
    @Config(qualifiers = TmrTestDevices.SMALL_PHONE_QUALIFIERS)
    fun allAcknowledged_onASmallPhone() {
        captureBothThemes(
            screenName = "ConsentAllAcknowledgedSmallPhone",
            uiState = baseState(acknowledged = ConsentPurpose.entries.toSet()),
            device = TmrTestDevices.smallPhone,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: ConsentUiState,
        device: TmrTestDevice = TmrTestDevices.board,
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
            TmrTheme(darkTheme = darkTheme.value) {
                ConsentScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = ConsentActions(
        onPurposeToggle = { },
        onAgree = {},
        onNotNow = {},
        onReadAgain = {},
        onBack = {},
    )

    private fun baseState(
        acknowledged: Set<ConsentPurpose> = emptySet(),
        isSaving: Boolean = false,
        isReadOnly: Boolean = false,
        isDeclined: Boolean = false,
        agreedAt: Instant? = null,
    ) = ConsentUiState(
        entries = consentPurposeStates().map { it.copy(isAcknowledged = it.purpose in acknowledged) },
        isSaving = isSaving,
        isReadOnly = isReadOnly,
        isDeclined = isDeclined,
        agreedAt = agreedAt,
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
    }
}
