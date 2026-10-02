package com.hirehop.feature.onboarding.impl.signin

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
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
class SignInScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "SignInDefault", uiState = baseState())
    }

    @Test
    fun adultConfirmed_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInAdultConfirmed",
            uiState = baseState(isAdultConfirmed = true),
        )
    }

    @Test
    fun adultNudge_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInAdultNudge",
            uiState = baseState(isAdultNudged = true),
        )
    }

    @Test
    fun inProgress_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInInProgress",
            uiState = baseState(stage = SignInStage.IN_PROGRESS, isAdultConfirmed = true),
        )
    }

    @Test
    fun success_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInSuccess",
            uiState = baseState(
                stage = SignInStage.SIGNED_IN,
                isAdultConfirmed = true,
                displayName = SignInAccount.localAccount.displayName,
            ),
        )
    }

    @Test
    fun providerUnavailable_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInProviderUnavailable",
            uiState = baseState(
                stage = SignInStage.FAILED,
                isAdultConfirmed = true,
                failure = SignInFailureReason.ProviderUnavailable,
            ),
        )
    }

    @Test
    fun networkUnavailable_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInNetworkUnavailable",
            uiState = baseState(
                stage = SignInStage.FAILED,
                isAdultConfirmed = true,
                failure = SignInFailureReason.NetworkUnavailable,
            ),
        )
    }

    @Test
    fun cancelled_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInCancelled",
            uiState = baseState(stage = SignInStage.CANCELLED, isAdultConfirmed = true),
        )
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInOffline",
            uiState = baseState(isOffline = true, isAdultConfirmed = true),
        )
    }

    @Test
    fun underEighteen_readsInLightAndDark() {
        captureBothThemes(
            screenName = "SignInUnderEighteen",
            uiState = baseState(stage = SignInStage.UNDER_18),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "SignInDefaultFont200",
            uiState = baseState(),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: SignInUiState,
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

    private fun showScreen(uiState: SignInUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                SignInScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = SignInActions(
        onAdultConfirmationChange = { },
        onReferralCodeChange = {},
        onContinue = {},
        onUnderEighteen = {},
        onBackFromUnderEighteen = {},
        onBack = {},
    )

    private fun baseState(
        stage: SignInStage = SignInStage.IDLE,
        isAdultConfirmed: Boolean = false,
        isAdultNudged: Boolean = false,
        isOffline: Boolean = false,
        failure: SignInFailureReason? = null,
        displayName: String? = null,
    ) = SignInUiState(
        stage = stage,
        isAdultConfirmed = isAdultConfirmed,
        isAdultNudged = isAdultNudged,
        isOffline = isOffline,
        failure = failure,
        displayName = displayName,
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
    }
}
