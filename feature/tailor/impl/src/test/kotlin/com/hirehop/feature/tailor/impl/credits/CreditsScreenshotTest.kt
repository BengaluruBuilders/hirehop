package com.hirehop.feature.tailor.impl.credits

import androidx.compose.runtime.Composable
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

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private const val ORDER_ID = "GPA.3318-4402-1187-55210"

private fun purchase(isPending: Boolean = false) = CreditsPurchaseEntry(
    orderId = ORDER_ID,
    credits = 5,
    formattedPrice = "₹149",
    formattedDate = "14 Apr 2027",
    isPending = isPending,
)

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class CreditsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun noPurchases_showsTheFreeApplication() {
        capture("CreditsNoPurchases", CreditsUiState(stage = CreditsStage.READY, freeCredits = 1))
    }

    @Test
    fun purchases_listsTheOrderWithItsId() {
        capture(
            "CreditsPurchases",
            CreditsUiState(stage = CreditsStage.READY, purchasedCredits = 4, purchases = listOf(purchase())),
        )
    }

    @Test
    fun pendingPurchase_addsNoCreditsYet() {
        capture(
            "CreditsPending",
            CreditsUiState(stage = CreditsStage.READY, purchases = listOf(purchase(isPending = true))),
        )
    }

    @Test
    fun offline_keepsTheHistoryAndDisablesBuying() {
        capture(
            "CreditsOffline",
            CreditsUiState(
                stage = CreditsStage.READY,
                purchasedCredits = 4,
                purchases = listOf(purchase()),
                isOffline = true,
            ),
        )
    }

    @Test
    fun loading_showsTheWheel() {
        capture("CreditsLoading", CreditsUiState(stage = CreditsStage.LOADING))
    }

    @Test
    fun error_offersARetry() {
        capture("CreditsError", CreditsUiState(stage = CreditsStage.ERROR))
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun purchases_atLargeTextStacksTheRows() {
        capture(
            "CreditsPurchasesFont200",
            CreditsUiState(stage = CreditsStage.READY, purchasedCredits = 4, purchases = listOf(purchase())),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun pendingPurchase_atLargeTextWrapsTheChip() {
        capture(
            "CreditsPendingFont200",
            CreditsUiState(stage = CreditsStage.READY, purchases = listOf(purchase(isPending = true))),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun capture(
        screenName: String,
        uiState: CreditsUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent { CreditsHost(uiState = uiState, dark = darkTheme.value) }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

@Composable
private fun CreditsHost(
    uiState: CreditsUiState,
    dark: Boolean,
) {
    HhTheme(darkTheme = dark) {
        CreditsScreen(
            uiState = uiState,
            actions = CreditsActions(
                onGetPack = {},
                onAskRefund = {},
                onContactHelp = {},
                onRetry = {},
                onNavigateBack = {},
            ),
        )
    }
}
