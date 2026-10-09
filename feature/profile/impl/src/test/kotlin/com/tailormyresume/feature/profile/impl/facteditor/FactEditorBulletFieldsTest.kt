package com.tailormyresume.feature.profile.impl.facteditor

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileLimits
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FactEditorBulletFieldsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val tooLongError = "Keep this shorter so it stays readable."

    private val state = FactEditorUiState(
        factId = "P-02",
        mode = FactEditorMode.Editing,
        outcome = FactEditorOutcome.Editing,
        draft = FactDraft(
            category = EntryCategory.PROJECT,
            title = "Placement Stats Dashboard",
            organization = "",
            startDate = "",
            endDate = "",
            detail = "Built the first screen.",
            moreBullets = listOf(
                EvidenceBullet("b-2", "Shipped it."),
                EvidenceBullet("b-3", "x".repeat(ProfileLimits.MAX_BULLET_LENGTH + 1)),
            ),
        ),
        fieldErrors = mapOf(FactField.DETAIL to FactDraftErrorReason.TOO_LONG),
        touchedFields = setOf(FactField.DETAIL),
        isDeleteDialogVisible = false,
        isOffline = false,
        isLoading = false,
        isSaving = false,
        isSaveFailed = false,
        wasQueued = false,
        provenance = FactSource.IMPORTED,
        isConfirmed = true,
    )

    private fun show() {
        composeRule.setContent { TmrTheme { FactEditorScreen(uiState = state, actions = FactEditorActions.None) } }
    }

    @Test
    fun onlyTheOverLimitBulletFieldShowsTheError() {
        show()

        composeRule.onAllNodesWithText(tooLongError).assertCountEquals(1)
    }

    @Test
    fun extraBulletFieldsCarryDistinctNumberedLabels() {
        show()

        composeRule.onNodeWithText("What you did").assertExists()
        composeRule.onNodeWithText("What you did, line 2").assertExists()
        composeRule.onNodeWithText("What you did, line 3").assertExists()
    }
}
