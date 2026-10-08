package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertWithMessage
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationStatusSheetRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun list_opensTheSharedStatusSheet() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationsScreen(
                    uiState = previewListState(
                        statusSheet = previewStatusSheet(),
                    ),
                    onAction = {},
                    now = PREVIEW_INSTANT,
                )
            }
        }

        assertSharedStatusSheetIsOpen()
    }

    @Test
    fun workspace_opensTheSharedStatusSheet() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationDetailScreen(
                    uiState = previewWorkspaceReadyState(statusSheet = previewStatusSheet()),
                    onAction = {},
                    now = PREVIEW_INSTANT,
                )
            }
        }

        assertSharedStatusSheetIsOpen()
    }

    private fun assertSharedStatusSheetIsOpen() {
        composeRule.onNodeWithText("Application status").assertIsDisplayed()
        composeRule.onNodeWithText("Save status").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        EXPECTED_STATUS_LABELS.forEach { label ->
            assertWithMessage("the sheet offers $label")
                .that(composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty())
                .isTrue()
        }
    }

    private companion object {
        val EXPECTED_STATUS_LABELS = listOf(
            "Saved",
            "Applied",
            "Interview",
            "Offer",
            "Rejected",
            "No response",
        )
    }

    private fun previewStatusSheet() = ApplicationStatusSheetState(
        rowId = "application-northwind-1",
        current = ApplicationStatus.APPLIED,
    )
}
