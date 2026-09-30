package com.hirehop.feature.tailor.impl.packpurchase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
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

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class PackPurchaseScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun ready_showsTheCatalogueAndThePrice() {
        capture("PackPurchaseReady", readyState())
    }

    @Test
    fun loading_showsTheWheelAndNoPrice() {
        capture(
            "PackPurchaseLoading",
            PackPurchaseUiState(
                stage = PackPurchaseStage.LOADING_PACKS,
                selectedPackId = ApplicationPack.APPLICATION_PACK_FIVE,
            ),
        )
    }

    @Test
    fun purchasing_saysNothingIsCharged() {
        capture("PackPurchasePurchasing", readyState().copy(stage = PackPurchaseStage.PURCHASING))
    }

    @Test
    fun pending_addsNoCredits() {
        capture(
            "PackPurchasePending",
            readyState().copy(
                stage = PackPurchaseStage.PENDING,
                entitlement = PurchaseEntitlement(
                    freeCredits = 1,
                    purchasedCredits = 0,
                    pendingPackIds = listOf(ApplicationPack.APPLICATION_PACK_FIVE),
                ),
            ),
        )
    }

    @Test
    fun success_saysNoPaymentWasTaken() {
        capture(
            "PackPurchaseSuccess",
            readyState().copy(
                stage = PackPurchaseStage.SUCCESS,
                entitlement = PurchaseEntitlement(
                    freeCredits = 1,
                    purchasedCredits = 5,
                    pendingPackIds = emptyList(),
                ),
            ),
        )
    }

    @Test
    fun cancelled_saysNoPaymentWasMade() {
        capture("PackPurchaseCancelled", readyState().copy(stage = PackPurchaseStage.CANCELLED))
    }

    @Test
    fun paymentDeclined_reportsTheRealReason() {
        capture(
            "PackPurchaseFailedDeclined",
            readyState().copy(
                stage = PackPurchaseStage.FAILED,
                failureReason = PurchaseFailureReason.PaymentDeclined,
            ),
        )
    }

    @Test
    fun purchaseUnavailable_reportsTheRealReason() {
        capture(
            "PackPurchaseFailedUnavailable",
            readyState().copy(
                stage = PackPurchaseStage.FAILED,
                failureReason = PurchaseFailureReason.PurchaseUnavailable,
            ),
        )
    }

    @Test
    fun restored_saysThereIsNothingToRestore() {
        capture("PackPurchaseRestored", readyState().copy(stage = PackPurchaseStage.RESTORED))
    }

    @Test
    fun offline_saysBuyingNeedsAConnection() {
        capture("PackPurchaseOffline", readyState().copy(stage = PackPurchaseStage.OFFLINE, isOffline = true))
    }

    @Test
    fun emptyCatalogue_saysNoPacksAreAvailable() {
        capture("PackPurchaseNoPacks", PackPurchaseUiState(stage = PackPurchaseStage.READY, packs = emptyList()))
    }

    @Test
    fun singleApplicationVariant_showsTheOneCreditPrice() {
        capture(
            "PackPurchaseSingleApplication",
            readyState().copy(selectedPackId = ApplicationPack.SINGLE_APPLICATION),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun ready_atLargeTextStacksFullWidth() {
        capture("PackPurchaseReadyFont200", readyState(), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: PackPurchaseUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            PackPurchaseHost(uiState = uiState, dark = darkTheme.value)
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

@Composable
private fun PackPurchaseHost(
    uiState: PackPurchaseUiState,
    dark: Boolean,
) {
    com.hirehop.core.designsystem.theme.HhTheme(darkTheme = dark) {
        PackPurchaseScreen(
            uiState = uiState,
            actions = PackPurchaseActions(
                onSelectPack = {},
                onBuy = {},
                onRestore = {},
                onDismiss = {},
                onNotNow = {},
                onNavigateBack = {},
            ),
        )
    }
}

private fun readyState(): PackPurchaseUiState = PackPurchaseUiState(
    stage = PackPurchaseStage.READY,
    packs = ApplicationPack.catalogue,
    selectedPackId = ApplicationPack.APPLICATION_PACK_FIVE,
    entitlement = PurchaseEntitlement(
        freeCredits = 1,
        purchasedCredits = 0,
        pendingPackIds = emptyList(),
    ),
)
