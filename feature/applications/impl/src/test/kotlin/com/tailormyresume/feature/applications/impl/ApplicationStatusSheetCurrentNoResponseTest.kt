package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationStatusSheetCurrentNoResponseTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noResponseRowIsShownAndSelectedWhenItIsTheCurrentStatus() {
        composeRule.setContent {
            TmrTheme(darkTheme = true) {
                ApplicationsScreen(
                    uiState = previewListState(
                        statusSheet = ApplicationStatusSheetState(
                            rowId = "application-northwind-1",
                            current = ApplicationStatus.NO_RESPONSE,
                        ),
                    ),
                    onAction = {},
                    now = PREVIEW_INSTANT,
                )
            }
        }

        composeRule.onNodeWithText("Application status").assertIsDisplayed()
        composeRule.onNode(hasText("No response") and isSelectable()).assertIsSelected()
    }
}
