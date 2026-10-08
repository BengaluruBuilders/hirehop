package com.tailormyresume.feature.settings.impl.yourdata

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
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class YourDataScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_readsInLightAndDark() {
        captureBothThemes(screenName = "YourDataDefault", uiState = content())
    }

    @Test
    fun preparing_readsInLightAndDark() {
        captureBothThemes(
            screenName = "YourDataPreparing",
            uiState = content(export = YourDataExport.PREPARING),
        )
    }

    @Test
    fun deleteDialog_readsInLightAndDark() {
        captureBothThemes(
            screenName = "YourDataDeleteDialog",
            uiState = content(deleteTarget = APPLICATIONS.last()),
        )
    }

    @Test
    fun offline_readsInLightAndDark() {
        captureBothThemes(screenName = "YourDataOffline", uiState = content(isOffline = true))
    }

    @Test
    fun exportFailed_readsInLightAndDark() {
        captureBothThemes(
            screenName = "YourDataExportFailed",
            uiState = content(export = YourDataExport.FAILED),
        )
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeText() {
        captureBothThemes(
            screenName = "YourDataDefaultFont200",
            uiState = content(),
            device = TmrTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: YourDataUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = runBlocking {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                YourDataScreen(uiState = uiState, actions = noActions)
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
        export: YourDataExport = YourDataExport.IDLE,
        isOffline: Boolean = false,
        deleteTarget: YourDataApplication? = null,
    ) = YourDataUiState.Content(
        profileFactCount = 18,
        confirmedFactCount = 15,
        userStatedFactCount = 3,
        applications = APPLICATIONS,
        purchases = listOf(
            YourDataPurchase(
                credits = 5,
                priceInPaise = 14_900L,
                currencyCode = "INR",
                purchasedAt = PURCHASE_TIME,
                isPending = false,
            ),
        ),
        isOffline = isOffline,
        export = export,
        deleteTarget = deleteTarget,
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
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        val PURCHASE_TIME = Instant.fromEpochSeconds(1_807_704_000)
        val APPLICATIONS = listOf(
            YourDataApplication("application-1", "Associate Analyst", "Northwind GCC"),
            YourDataApplication("application-2", "Data Analyst", "Paisa Ledger (start-up)"),
            YourDataApplication("application-3", "Operations Analyst", "Sahyadri Motors"),
            YourDataApplication("application-4", "Business Analyst", "Meridian GCC"),
        )
    }
}
