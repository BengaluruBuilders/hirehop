package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
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
class PasteJobDescriptionBlankLabelsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun typingBlankLabels_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionTypingBlankLabels",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = SAMPLE_JD,
            ),
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: PasteJobDescriptionUiState,
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

    private fun showScreen(uiState: PasteJobDescriptionUiState) {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                PasteJobDescriptionScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = PasteJobDescriptionActions(
        onTextChange = {},
        onPaste = {},
        onCompanyChange = {},
        onRoleChange = {},
        onClear = {},
        onAnalyse = {},
        onRetry = {},
        onBack = {},
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        const val FREE_LEFT = 2

        val SAMPLE_JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, and model dashboards in Power BI or Tableau. The team works in Agile with " +
            "JIRA and reports to stakeholders every Friday morning."
    }
}
