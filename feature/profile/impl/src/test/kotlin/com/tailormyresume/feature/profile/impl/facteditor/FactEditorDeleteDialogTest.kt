package com.tailormyresume.feature.profile.impl.facteditor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FactEditorDeleteDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

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
            detail = "",
        ),
        fieldErrors = emptyMap(),
        touchedFields = emptySet(),
        isDeleteDialogVisible = true,
        isOffline = false,
        isLoading = false,
        isSaving = false,
        isSaveFailed = false,
        wasQueued = false,
        provenance = FactSource.USER_EDITED,
        isConfirmed = true,
    )

    @Test
    fun deleteTitleNamesOnlyTheFactId() {
        composeRule.setContent { TmrTheme { FactEditorScreen(uiState = state, actions = FactEditorActions.None) } }

        composeRule.onNodeWithText("Delete P-02?").assertIsDisplayed()
    }

    @Test
    fun deleteDialogShowsTheErrorIconTile() {
        composeRule.setContent { TmrTheme { FactEditorScreen(uiState = state, actions = FactEditorActions.None) } }

        composeRule.onNodeWithTag("confirmIconTile").assertIsDisplayed()
    }
}
