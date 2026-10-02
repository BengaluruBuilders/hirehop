package com.hirehop.feature.tailor.impl.packpurchase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.offline.MockPackCatalogue
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

private fun readyState(packs: List<ApplicationPack> = listOf(MockPackCatalogue.applicationPackFive)) =
    PackPurchaseUiState(
        stage = PackPurchaseStage.READY,
        packs = packs,
        selectedPackId = ApplicationPack.APPLICATION_PACK_FIVE,
        jobTitle = "Associate Analyst",
        jobCompany = "Northwind GCC",
        totalCredits = 0,
    )

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class PackPurchaseScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun default_showsThePriceAndTheEqualWeightNotNow() {
        capture("PackPurchaseDefault", readyState())
    }

    @Test
    fun purchasing_waitsForGooglePlay() {
        capture("PackPurchasePurchasing", readyState().copy(stage = PackPurchaseStage.PURCHASING))
    }

    @Test
    fun pending_addsNoCreditsYet() {
        capture("PackPurchasePending", readyState().copy(stage = PackPurchaseStage.PENDING))
    }

    @Test
    fun success_countsFromZeroToFive() {
        capture(
            "PackPurchaseSuccess",
            readyState().copy(
                stage = PackPurchaseStage.SUCCESS,
                creditsBefore = 0,
                totalCredits = 5,
                receipt = PackPurchaseReceipt(credits = 5, formattedPrice = "₹149", formattedDate = "14 Apr 2027"),
            ),
        )
    }

    @Test
    fun fromCredits_usesTheCreditsHeaderAndTheGenericHeadline() {
        capture(
            "PackPurchaseFromCredits",
            readyState().copy(hasApplication = false, jobTitle = "", jobCompany = ""),
        )
    }

    @Test
    fun fromCredits_successGoesBackToCredits() {
        capture(
            "PackPurchaseFromCreditsSuccess",
            readyState().copy(
                hasApplication = false,
                jobTitle = "",
                jobCompany = "",
                stage = PackPurchaseStage.SUCCESS,
                creditsBefore = 0,
                totalCredits = 5,
                receipt = PackPurchaseReceipt(credits = 5, formattedPrice = "₹149", formattedDate = "14 Apr 2027"),
            ),
        )
    }

    @Test
    fun cancelled_saysNoPaymentWasMade() {
        capture("PackPurchaseCancelled", readyState().copy(stage = PackPurchaseStage.CANCELLED))
    }

    @Test
    fun failed_offersTryAgainAndNotNow() {
        capture(
            "PackPurchaseFailed",
            readyState().copy(
                stage = PackPurchaseStage.FAILED,
                failureReason = PurchaseFailureReason.PaymentDeclined,
            ),
        )
    }

    @Test
    fun offline_disablesTheBuyButton() {
        capture("PackPurchaseOffline", readyState().copy(isOffline = true))
    }

    @Test
    fun secondPack_showsAsAnOutlineButton() {
        capture("PackPurchaseSecondPack", readyState(packs = MockPackCatalogue.all))
    }

    @Test
    fun catalogueUnavailable_offersARetry() {
        capture("PackPurchaseCatalogueUnavailable", PackPurchaseUiState(stage = PackPurchaseStage.FAILED))
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun default_atLargeTextShowsOnlyThePriceAndTheButtons() {
        capture("PackPurchaseDefaultFont200", readyState(), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: PackPurchaseUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent { PackPurchaseHost(uiState = uiState, dark = darkTheme.value) }
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
    HhTheme(darkTheme = dark) {
        PackPurchaseScreen(
            uiState = uiState,
            actions = PackPurchaseActions(
                onBuy = {},
                onRetryBuy = {},
                onReloadPacks = {},
                onNotNow = {},
                onBackToPreview = {},
                onDownloadAfterPurchase = {},
                onOpenCredits = {},
                onNavigateBack = {},
            ),
        )
    }
}
