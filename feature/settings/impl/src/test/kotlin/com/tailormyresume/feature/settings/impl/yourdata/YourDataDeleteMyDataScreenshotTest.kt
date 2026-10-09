package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
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
class YourDataDeleteMyDataScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun confirmDialog_readsInLightAndDark() {
        captureBothThemes(screenName = "YourDataDeleteMyDataDialog", deletion = YourDataDeletion.CONFIRMING)
    }

    @Test
    fun failed_readsInLightAndDark() {
        captureBothThemes(screenName = "YourDataDeleteMyDataFailed", deletion = YourDataDeletion.FAILED)
    }

    private fun captureBothThemes(screenName: String, deletion: YourDataDeletion) = runBlocking {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                YourDataScreen(uiState = content(deletion), actions = noActions)
            }
        }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = TmrTestDevices.board,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun content(deletion: YourDataDeletion) = YourDataUiState.Content(
        profileFactCount = 18,
        confirmedFactCount = 15,
        userStatedFactCount = 3,
        applications = APPLICATIONS,
        purchases = emptyList(),
        isOffline = false,
        export = YourDataExport.IDLE,
        deleteTarget = null,
        deletion = deletion,
    )

    private val noActions = YourDataActions(
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

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        val APPLICATIONS = listOf(
            YourDataApplication("application-1", "Associate Analyst", "Northwind GCC"),
            YourDataApplication("application-2", "Data Analyst", "Paisa Ledger (start-up)"),
            YourDataApplication("application-3", "Operations Analyst", "Sahyadri Motors"),
            YourDataApplication("application-4", "Business Analyst", "Meridian GCC"),
        )
    }
}
