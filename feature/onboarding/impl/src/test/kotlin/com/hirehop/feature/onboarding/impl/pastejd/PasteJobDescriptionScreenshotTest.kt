package com.hirehop.feature.onboarding.impl.pastejd

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
class PasteJobDescriptionScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun empty_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionEmpty",
            uiState = PasteJobDescriptionUiState(freeAnalysesLeft = FREE_LEFT),
        )
    }

    @Test
    fun typing_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionTyping",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = SAMPLE_JD,
                company = "Northwind GCC",
                role = "Associate Analyst",
            ),
        )
    }

    @Test
    fun tooShort_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionTooShort",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = "Associate Analyst",
            ),
        )
    }

    @Test
    fun linkOnly_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionLinkOnly",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = "https://jobs.example.com/role/1",
            ),
        )
    }

    @Test
    fun overLong_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionOverLong",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = "word ".repeat(PASTE_JD_MAX_CHARACTERS),
            ),
        )
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionOffline",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                isOffline = true,
                text = SAMPLE_JD,
                company = "Northwind GCC",
                role = "Associate Analyst",
            ),
        )
    }

    @Test
    fun sharedIn_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionSharedIn",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = SAMPLE_JD,
                company = "Northwind GCC",
                role = "Associate Analyst",
                arrival = PasteJobDescriptionArrival.SHARED_IN,
            ),
        )
    }

    @Test
    fun dailyLimitReached_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionDailyLimit",
            uiState = PasteJobDescriptionUiState(
                text = SAMPLE_JD,
                company = "Northwind GCC",
                role = "Associate Analyst",
                freeAnalysesLeft = 0,
            ),
        )
    }

    @Test
    fun loading_readsInLightAndDark() {
        captureBothThemes(
            screenName = "PasteJobDescriptionLoading",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                isLoading = true,
            ),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun typing_atLargeTextStacksTheFieldsFullWidth() {
        captureBothThemes(
            screenName = "PasteJobDescriptionTypingFont200",
            uiState = PasteJobDescriptionUiState(
                freeAnalysesLeft = FREE_LEFT,
                text = SAMPLE_JD,
                company = "Northwind GCC",
                role = "Associate Analyst",
            ),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: PasteJobDescriptionUiState,
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

    private fun showScreen(uiState: PasteJobDescriptionUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
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
