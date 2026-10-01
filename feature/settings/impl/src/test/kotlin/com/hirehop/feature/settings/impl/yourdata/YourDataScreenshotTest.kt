package com.hirehop.feature.settings.impl.yourdata

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.sampleJobDescription
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class YourDataScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "YourDataDefault", uiState = baseState())
    }

    @Test
    fun preparing_readsInLightAndDark() {
        captureBothThemes(
            screenName = "YourDataPreparing",
            uiState = baseState(
                stage = YourDataStage.PREPARING,
                steps = yourDataExportSteps(currentIndex = 1, applicationCount = APPLICATION_COUNT),
            ),
        )
    }

    @Test
    fun ready_readsInLightAndDark() {
        captureBothThemes(
            screenName = "YourDataReady",
            uiState = baseState(
                stage = YourDataStage.READY,
                exportFileName = "HireHop-data_Priya-Deshmukh.zip",
            ),
        )
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(
            screenName = "YourDataOffline",
            uiState = baseState(isOffline = true),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "YourDataDefaultFont200",
            uiState = baseState(),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun preparing_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "YourDataPreparingFont200",
            uiState = baseState(
                stage = YourDataStage.PREPARING,
                steps = yourDataExportSteps(currentIndex = 1, applicationCount = APPLICATION_COUNT),
            ),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    fun deleteDialog_isCoveredByTheViewModelTestBecauseADialogIsASeparateWindow() {
        val target = YourDataDeleteTarget(
            applicationId = "application-northwind-1",
            title = "Associate Android Engineer",
            company = "Northwind GCC",
        )

        composeRule.setContent {
            HhTheme(darkTheme = false) {
                YourDataDeleteDialog(
                    target = target,
                    profileFactCount = 18,
                    onConfirm = {},
                    onDismiss = {},
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: YourDataUiState,
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

    private fun showScreen(uiState: YourDataUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                YourDataScreen(
                    uiState = uiState,
                    actions = YourDataActions(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun baseState(
        stage: YourDataStage = YourDataStage.IDLE,
        steps: List<YourDataExportStep> = emptyList(),
        exportFileName: String? = null,
        isOffline: Boolean = false,
    ): YourDataUiState = YourDataUiState(
        ledger = yourDataLedger(
            profile = canonicalCandidateProfile,
            applications = applications(),
            entitlement = PurchaseEntitlement(
                freeCredits = 0,
                purchasedCredits = 5,
                pendingPackIds = listOf("application_pack_5"),
            ),
        ),
        stage = stage,
        steps = steps,
        exportFileName = exportFileName,
        isOffline = isOffline,
        profileFactCount = canonicalCandidateProfile.entries.size,
    )

    private fun applications() = List(APPLICATION_COUNT) { index ->
        canonicalApplication.copy(
            id = "application-$index",
            job = sampleJobDescription.copy(
                title = APPLICATION_TITLES[index],
                company = APPLICATION_COMPANIES[index],
            ),
        )
    }

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        const val APPLICATION_COUNT = 4
        val APPLICATION_TITLES = listOf(
            "Associate Android Engineer",
            "Data Analyst Intern",
            "Graduate Engineer Trainee",
            "Business Analyst",
        )
        val APPLICATION_COMPANIES = listOf(
            "Northwind GCC",
            "Paisa Ledger",
            "Sahyadri Motors",
            "Meridian GCC",
        )
    }
}
