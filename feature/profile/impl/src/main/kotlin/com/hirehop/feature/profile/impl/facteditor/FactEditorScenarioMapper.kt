package com.hirehop.feature.profile.impl.facteditor

import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

object FactEditorScenarioMapper {

    fun seed(
        scenario: DebugScenario,
        category: EntryCategory,
        entryId: String?,
    ): FactEditorUiState = FactEditorUiState(
        factId = entryId.orEmpty(),
        mode = if (entryId == null) FactEditorMode.New else FactEditorMode.Editing,
        outcome = if (scenario == DebugScenario.SUCCESS) FactEditorOutcome.Saved else FactEditorOutcome.Editing,
        draft = blankDraft(category),
        fieldErrors = emptyMap(),
        touchedFields = emptySet(),
        isDeleteDialogVisible = false,
        isOffline = scenario == DebugScenario.OFFLINE,
        isLoading = scenario == DebugScenario.LOADING,
        isSaving = false,
        isSaveFailed = scenario == DebugScenario.ERROR,
        wasQueued = false,
        provenance = FactSource.USER_STATED,
        isConfirmed = true,
    )

    fun blankDraft(category: EntryCategory): FactDraft = FactDraft(
        category = category,
        title = "",
        organization = "",
        startDate = "",
        endDate = "",
        detail = "",
    )

    fun withEntry(
        state: FactEditorUiState,
        entry: ProfileEntry,
        scenario: DebugScenario,
        displayId: String = entry.id,
    ): FactEditorUiState = state.copy(
        factId = entry.id,
        displayId = displayId,
        mode = FactEditorMode.Editing,
        draft = entry.toFactDraft(),
        isLoading = false,
        isConfirmed = if (scenario == DebugScenario.PARTIAL) false else entry.isConfirmed,
        provenance = entry.source,
    )

    fun withoutEntry(
        state: FactEditorUiState,
        nextId: String,
    ): FactEditorUiState = state.copy(
        factId = nextId,
        displayId = nextId,
        mode = FactEditorMode.New,
        draft = blankDraft(state.draft.category),
        fieldErrors = emptyMap(),
        touchedFields = emptySet(),
        isLoading = false,
    )
}
