package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationDeleteDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTheCanvasTitleAndCancelDeleteActions() {
        var confirmed = 0
        var cancelled = 0
        composeRule.setContent {
            TmrTheme(darkTheme = true) {
                ApplicationDeleteDialog(
                    jobTitle = NORTHWIND_ROLE,
                    company = NORTHWIND_COMPANY,
                    scope = WorkspaceDeleteScope(
                        hasJobDescription = true,
                        hasGapAnalysis = true,
                        hasTailoredResume = true,
                        hasNotes = true,
                        prepTaskCount = 3,
                        profileFactCount = 18,
                        creditCount = 4,
                    ),
                    onConfirm = { confirmed++ },
                    onCancel = { cancelled++ },
                )
            }
        }

        composeRule.onNodeWithText("Delete this application?").assertIsDisplayed()
        composeRule.onNodeWithText("Keep application").assertDoesNotExist()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        assertThat(cancelled).isEqualTo(1)
        assertThat(confirmed).isEqualTo(1)
    }

    @Test
    fun cancelAndDeleteButtonsHaveEqualHeights() {
        showDialog()

        val cancel = composeRule.onNodeWithText("Cancel").getUnclippedBoundsInRoot()
        composeRule.onNodeWithText("Delete").assertHeightIsEqualTo(cancel.bottom - cancel.top)
    }

    @Test
    fun titleIsAHeadingAndThePanelHasAPaneTitle() {
        showDialog()

        composeRule.onNodeWithText("Delete this application?")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle)).assertIsDisplayed()
    }

    private fun showDialog() {
        composeRule.setContent {
            TmrTheme(darkTheme = true) {
                ApplicationDeleteDialog(
                    jobTitle = NORTHWIND_ROLE,
                    company = NORTHWIND_COMPANY,
                    scope = WorkspaceDeleteScope(
                        hasJobDescription = true,
                        hasGapAnalysis = true,
                        hasTailoredResume = true,
                        hasNotes = true,
                        prepTaskCount = 3,
                        profileFactCount = 18,
                        creditCount = 4,
                    ),
                    onConfirm = {},
                    onCancel = {},
                )
            }
        }
    }
}
