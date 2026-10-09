package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YourDataCancelExportTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cancelOnThePreparingScreenCancelsTheExport() {
        var cancelled = 0
        composeRule.setContent {
            TmrTheme {
                YourDataScreen(
                    uiState = YourDataUiState.Content(
                        profileFactCount = 18,
                        confirmedFactCount = 15,
                        userStatedFactCount = 3,
                        applications = emptyList(),
                        purchases = emptyList(),
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
                        onCancelExport = { cancelled += 1 },
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Cancel").performClick()

        assertThat(cancelled).isEqualTo(1)
    }
}
