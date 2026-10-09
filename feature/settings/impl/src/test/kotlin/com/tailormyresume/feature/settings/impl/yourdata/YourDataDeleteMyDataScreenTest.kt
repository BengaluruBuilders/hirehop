package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
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
class YourDataDeleteMyDataScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var requests = 0
    private var confirms = 0
    private var dismissals = 0

    private fun show(
        isOffline: Boolean = false,
        deletion: YourDataDeletion = YourDataDeletion.IDLE,
    ) {
        composeRule.setContent {
            TmrTheme {
                YourDataScreen(
                    uiState = YourDataUiState.Content(
                        profileFactCount = 18,
                        confirmedFactCount = 15,
                        userStatedFactCount = 3,
                        applications = List(4) { index -> YourDataApplication("application-$index", "Analyst", "Acme") },
                        purchases = emptyList(),
                        isOffline = isOffline,
                        export = YourDataExport.IDLE,
                        deleteTarget = null,
                        deletion = deletion,
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
                        onDeleteMyData = { requests++ },
                        onDeleteMyDataConfirm = { confirms++ },
                        onDeleteMyDataDismiss = { dismissals++ },
                    ),
                )
            }
        }
    }

    @Test
    fun showsTheDeleteMyDataButtonAndTheNoBirthDateNote() {
        show()

        composeRule.onNodeWithText("Delete my data").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithText("Download my data").assertIsDisplayed()
        composeRule.onNodeWithText("TailorMyResume keeps no date of birth and no photo.").assertIsDisplayed()
    }

    @Test
    fun tappingTheButtonAsksForTheRequest() {
        show()

        composeRule.onNodeWithText("Delete my data").performClick()

        assertThat(requests).isEqualTo(1)
        assertThat(confirms).isEqualTo(0)
    }

    @Test
    fun offline_disablesBothButtons() {
        show(isOffline = true)

        composeRule.onNodeWithText("Delete my data").assertIsNotEnabled()
        composeRule.onNodeWithText("Download my data").assertIsNotEnabled()
    }

    @Test
    fun whileDeleting_disablesTheButton() {
        show(deletion = YourDataDeletion.DELETING)

        composeRule.onNodeWithText("Delete my data").assertIsNotEnabled()
    }

    @Test
    fun confirmationDialogShowsTheRealCountsAndSaysTheAccountAndCreditsStay() {
        show(deletion = YourDataDeletion.CONFIRMING)

        composeRule.onNodeWithText("Delete my data?").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Your 18 facts and 4 applications are deleted. Your account and credits stay.",
        ).assertIsDisplayed()
    }

    @Test
    fun confirmationDialogCancelDeletesNothing() {
        show(deletion = YourDataDeletion.CONFIRMING)

        composeRule.onNodeWithText("Cancel").performClick()

        assertThat(dismissals).isEqualTo(1)
        assertThat(confirms).isEqualTo(0)
    }

    @Test
    fun confirmationDialogDeleteConfirms() {
        show(deletion = YourDataDeletion.CONFIRMING)

        composeRule.onNode(hasText("Delete") and hasAnyAncestor(isDialog())).performClick()

        assertThat(confirms).isEqualTo(1)
    }

    @Test
    fun failedDeletionShowsATruthfulNotice() {
        show(deletion = YourDataDeletion.FAILED)

        composeRule.onNodeWithText("couldn't delete all your data", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Delete my data").assertIsEnabled()
    }
}
