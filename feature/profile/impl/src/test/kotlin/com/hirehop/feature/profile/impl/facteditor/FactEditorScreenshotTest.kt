package com.hirehop.feature.profile.impl.facteditor

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.screenshot.HH_THEME_DARK
import com.hirehop.core.screenshot.HH_THEME_LIGHT
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureForDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FactEditorScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun newFact() = runTest {
        assertBothThemes(screenName = "fact_editor_new", state = newFactState())
    }

    @Test
    fun editingFact() = runTest {
        assertBothThemes(screenName = "fact_editor_editing", state = editingFactState())
    }

    @Test
    fun validationError() = runTest {
        assertBothThemes(screenName = "fact_editor_validation_error", state = validationErrorState())
    }

    @Test
    fun offline() = runTest {
        assertBothThemes(screenName = "fact_editor_offline", state = offlineState())
    }

    @Test
    fun editingFact_atTwoHundredPercentText() = runTest {
        RuntimeEnvironment.setFontScale(HhTestDevices.LARGE_FONT_SCALE)
        assertBothThemes(
            screenName = "fact_editor_editing_font_200",
            state = editingFactState(),
        )
        RuntimeEnvironment.setFontScale(HhTestDevices.DEFAULT_FONT_SCALE)
    }

    @Test
    fun editingFact_onASmallPhone() = runTest {
        val darkTheme = mutableStateOf(true)
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                FactEditorScreen(uiState = editingFactState(), actions = noOpActions())
            }
        }
        composeRule.captureForDevices(
            outputDirectory = SCREENSHOT_DIRECTORY,
            screenName = "fact_editor_editing_small_phone",
            devices = listOf(HhTestDevices.smallPhone),
            theme = HH_THEME_DARK,
        )
    }

    @Test
    fun deleteDialog() = runTest {
        val darkTheme = mutableStateOf(false)
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                FactEditorScreen(uiState = deleteDialogState(), actions = noOpActions())
            }
        }
        captureDialogTheme(dark = false)
        darkTheme.value = true
        composeRule.waitForIdle()
        captureDialogTheme(dark = true)
    }

    private suspend fun assertBothThemes(screenName: String, state: FactEditorUiState) {
        val darkTheme = mutableStateOf(false)
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                FactEditorScreen(uiState = state, actions = noOpActions())
            }
        }
        composeRule.captureMultiTheme(
            outputDirectory = SCREENSHOT_DIRECTORY,
            screenName = screenName,
            device = HhTestDevices.board,
            setTheme = { dark -> darkTheme.value = dark },
        )
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun captureDialogTheme(dark: Boolean) {
        captureScreenRoboImage(
            File(SCREENSHOT_DIRECTORY, "fact_editor_delete_dialog_board_${themeName(dark)}.png").path,
        )
    }

    private fun themeName(dark: Boolean) = if (dark) HH_THEME_DARK else HH_THEME_LIGHT

    private fun noOpActions() = FactEditorActions(
        onTitleChange = {},
        onDetailChange = {},
        onToolsChange = {},
        onStartDateChange = {},
        onEndDateChange = {},
        onSave = {},
        onCancel = {},
        onRequestDelete = {},
        onConfirmDelete = {},
        onDismissDelete = {},
    )

    private fun newFactState() = seedState(
        factId = FactIdAllocator().nextId(EntryCategory.PROJECT, listOf(PLACEHOLDER_ENTRY)),
        mode = FactEditorMode.New,
        draft = FactDraft(
            category = EntryCategory.PROJECT,
            title = "",
            organization = "",
            startDate = "",
            endDate = "",
            detail = "",
        ),
    )

    private fun editingFactState() = seedState(
        factId = FactIdAllocator().nextId(EntryCategory.PROJECT, emptyList()),
        mode = FactEditorMode.Editing,
        draft = FactDraft(
            category = EntryCategory.PROJECT,
            title = "Placement Stats Dashboard",
            organization = "Power BI, Excel",
            startDate = "Jan 2024",
            endDate = "Apr 2024",
            detail = "Built a dashboard of 3 batches of placement data for the college T&P cell.",
        ),
    )

    private fun validationErrorState() = editingFactState().copy(
        draft = editingFactState().draft.copy(endDate = "Dec 2023"),
        fieldErrors = mapOf(FactField.END_DATE to FactDraftErrorReason.END_BEFORE_START),
        touchedFields = setOf(FactField.END_DATE),
    )

    private fun deleteDialogState() = editingFactState().copy(isDeleteDialogVisible = true)

    private fun offlineState() = editingFactState().copy(isOffline = true)

    private fun seedState(
        factId: String,
        mode: FactEditorMode,
        draft: FactDraft,
    ) = FactEditorUiState(
        factId = factId,
        mode = mode,
        outcome = FactEditorOutcome.Editing,
        draft = draft,
        fieldErrors = emptyMap(),
        touchedFields = emptySet(),
        isDeleteDialogVisible = false,
        isOffline = false,
        isLoading = false,
        isSaving = false,
        isSaveFailed = false,
        wasQueued = false,
        provenance = if (mode == FactEditorMode.New) FactSource.USER_STATED else FactSource.USER_EDITED,
        isConfirmed = true,
    )

    private companion object {
        const val SCREENSHOT_DIRECTORY = "src/test/screenshots"
        val PLACEHOLDER_ENTRY = ProfileEntry(
            id = FactIdAllocator().nextId(EntryCategory.PROJECT, emptyList()),
            category = EntryCategory.PROJECT,
            title = "",
            organization = "",
            startDate = "",
            endDate = "",
            bullets = emptyList(),
            source = FactSource.USER_STATED,
            isConfirmed = true,
        )
    }
}
