package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YourDataPreparingPurchasesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showPreparing(purchasesKnown: Boolean) {
        composeRule.setContent {
            TmrTheme {
                YourDataScreen(
                    uiState = YourDataUiState.Content(
                        profileFactCount = 18,
                        confirmedFactCount = 15,
                        userStatedFactCount = 3,
                        applications = emptyList(),
                        purchases = emptyList(),
                        purchasesKnown = purchasesKnown,
                        isOffline = false,
                        export = YourDataExport.PREPARING,
                        deleteTarget = null,
                    ),
                    actions = YourDataActions(
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
                    ),
                )
            }
        }
    }

    @Test
    fun unknownHistoryNamesNoPurchaseCount() {
        showPreparing(purchasesKnown = false)

        assertThat(composeRule.onAllNodesWithText("Adding 0 purchase records").fetchSemanticsNodes()).isEmpty()
        assertThat(composeRule.onAllNodesWithText("Adding your purchase records").fetchSemanticsNodes()).isNotEmpty()
    }

    @Test
    fun knownEmptyHistoryStillNamesTheCount() {
        showPreparing(purchasesKnown = true)

        assertThat(composeRule.onAllNodesWithText("Adding 0 purchase records").fetchSemanticsNodes()).isNotEmpty()
    }
}
