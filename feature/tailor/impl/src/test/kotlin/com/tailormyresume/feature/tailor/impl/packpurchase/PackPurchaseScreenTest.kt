package com.tailormyresume.feature.tailor.impl.packpurchase

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.domain.offline.MockPackCatalogue
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private val BUY_DESCRIPTION = hasContentDescription("Buy 5 applications", substring = true)

private const val ONE_TIME_LINE = "One-time payment through Google Play. Price includes GST."
private const val EACH_APPLICATION_LINE = "Each application: a tailored resume, its export, and prep questions."
private const val REFUNDS_LINK = "Refunds and help"
private const val HEADLINE = "Your Northwind GCC resume is ready"
private const val NO_SUBSCRIPTION_LINE = "No subscription. Nothing renews."

private fun readyState() = PackPurchaseUiState(
    stage = PackPurchaseStage.READY,
    packs = listOf(MockPackCatalogue.applicationPackFive),
    selectedPackId = ApplicationPack.APPLICATION_PACK_FIVE,
    jobTitle = "Associate Analyst",
    jobCompany = "Northwind GCC",
    totalCredits = 0,
)

private fun successState() = readyState().copy(
    stage = PackPurchaseStage.SUCCESS,
    totalCredits = 5,
    creditsBefore = 0,
    receipt = PackPurchaseReceipt(credits = 5, formattedPrice = "₹149", formattedDate = "14 Apr 2027"),
)

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class PackPurchaseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(uiState: PackPurchaseUiState, onBuy: (String) -> Unit = {}) {
        composeRule.setContent {
            TmrTheme {
                PackPurchaseScreen(
                    uiState = uiState,
                    actions = PackPurchaseActions(
                        onBuy = onBuy,
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
    }

    @Test
    fun buyButtonDoesNothingWhenOffline() {
        val bought = mutableListOf<String>()
        show(readyState().copy(isOffline = true), onBuy = { bought += it })
        composeRule.onNode(BUY_DESCRIPTION).performClick()
        assertTrue(bought.isEmpty())
    }

    @Test
    fun buyButtonBuysWhenOnline() {
        val bought = mutableListOf<String>()
        show(readyState(), onBuy = { bought += it })
        composeRule.onNode(BUY_DESCRIPTION).performClick()
        assertEquals(listOf(ApplicationPack.APPLICATION_PACK_FIVE), bought)
    }

    @Test
    fun anUnconfirmedPaymentNeverSaysNoChargeWasAdded() {
        show(readyState().copy(stage = PackPurchaseStage.FAILED, failureReason = PurchaseFailureReason.PaymentUnconfirmed))
        composeRule.onNodeWithText("We could not confirm your payment", substring = true).assertExists()
        composeRule.onNode(hasText("added no charge", substring = true)).assertDoesNotExist()
    }

    @Test
    fun successOffersPdfDownloadByDefault() {
        show(successState())
        composeRule.onNodeWithText("Download PDF").assertExists()
        composeRule.onNodeWithText("Download DOCX").assertDoesNotExist()
    }

    @Test
    fun successOffersDocxDownloadWhenDocxWasPicked() {
        show(successState().copy(format = ExportFormat.DOCX))
        composeRule.onNodeWithText("Download DOCX").assertExists()
        composeRule.onNodeWithText("Download PDF").assertDoesNotExist()
    }

    @Test
    fun priceDisclosureStaysAtLargeText() {
        composeRule.setContent {
            TmrTheme {
                CompositionLocalProvider(
                    LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f),
                ) {
                    PackPurchaseScreen(
                        uiState = readyState(),
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
        }
        composeRule.onNodeWithText(ONE_TIME_LINE).assertExists()
        composeRule.onNodeWithText(NO_SUBSCRIPTION_LINE).assertExists()
        composeRule.onNodeWithText(EACH_APPLICATION_LINE).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(REFUNDS_LINK).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(HEADLINE, substring = true).assertExists()
    }
}
