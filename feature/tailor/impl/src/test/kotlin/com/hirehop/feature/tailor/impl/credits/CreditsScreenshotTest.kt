package com.hirehop.feature.tailor.impl.credits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.domain.ApplicationPack
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
class CreditsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun freeOnly_showsTheFreeBucketOnItsOwn() {
        capture("CreditsFreeOnly", state(stage = CreditsStage.FREE_ONLY, free = 1, purchased = 0))
    }

    @Test
    fun purchasedOnly_showsThePurchasedBucketOnItsOwn() {
        capture("CreditsPurchasedOnly", state(stage = CreditsStage.PURCHASED_ONLY, free = 0, purchased = 5))
    }

    @Test
    fun mixed_neverMergesTheTwoBuckets() {
        capture("CreditsMixed", state(stage = CreditsStage.MIXED, free = 2, purchased = 5))
    }

    @Test
    fun zero_carriesNoShame() {
        capture("CreditsZero", state(stage = CreditsStage.ZERO, free = 0, purchased = 0))
    }

    @Test
    fun pending_showsAPackWaitingToClear() {
        capture(
            "CreditsPending",
            state(stage = CreditsStage.PENDING, free = 1, purchased = 0).copy(
                pendingPackIds = listOf(ApplicationPack.APPLICATION_PACK_FIVE),
                purchases = listOf(
                    CreditsPurchaseEntry(
                        packId = ApplicationPack.APPLICATION_PACK_FIVE,
                        packName = ApplicationPack.applicationPackFive.name,
                        credits = ApplicationPack.applicationPackFive.credits,
                        status = CreditsPurchaseStatus.PENDING,
                        formattedPrice = "₹149.00",
                        creditsExpire = ApplicationPack.applicationPackFive.creditsExpire,
                    ),
                ),
            ),
        )
    }

    @Test
    fun offline_saysThisIsTheSavedHistory() {
        capture(
            "CreditsOffline",
            state(stage = CreditsStage.OFFLINE, free = 1, purchased = 5).copy(isOffline = true),
        )
    }

    @Test
    fun loading_readsTheSavedNumbers() {
        capture("CreditsLoading", CreditsUiState(stage = CreditsStage.LOADING))
    }

    @Test
    fun error_saysNothingWasCharged() {
        capture("CreditsError", CreditsUiState(stage = CreditsStage.ERROR))
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun mixed_atLargeTextStacksFullWidth() {
        capture("CreditsMixedFont200", state(stage = CreditsStage.MIXED, free = 2, purchased = 5), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: CreditsUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            CreditsHost(uiState = uiState, dark = darkTheme.value)
        }
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
    com.hirehop.core.designsystem.theme.HhTheme(darkTheme = dark) {
        CreditsScreen(
            uiState = uiState,
            actions = CreditsActions(
                onRestore = {},
                onDismiss = {},
                onNavigateBack = {},
            ),
        )
    }
}

private fun state(
    stage: CreditsStage,
    free: Int,
    purchased: Int,
) = CreditsUiState(
    stage = stage,
    freeCredits = free,
    purchasedCredits = purchased,
    purchasedCreditsNeverExpire = true,
)
